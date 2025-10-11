package com.example.depremapp.data

import android.graphics.Bitmap
import android.util.Base64
import com.example.depremapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GroqRepository {
    
    private val api = GeminiRetrofitInstance.api
    
    suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult = withContext(Dispatchers.IO) {
        try {
            // Detaylı çatlak analizi
            val crackData = analyzeCrackDetails(bitmap)
            val complexity = crackData.complexity
            
            // Hasar tipi belirleme
            val (damageType, crackWidth, recommendation) = when {
                complexity < 0.15 -> Triple(
                    DamageType.TYPE_O,
                    String.format("%.1f mm", crackData.estimatedWidth),
                    "Yapıda hasar tespit edilmedi. Rutin kontroller yeterlidir."
                )
                complexity < 0.30 -> Triple(
                    DamageType.TYPE_A,
                    String.format("%.1f mm", crackData.estimatedWidth),
                    "Kılcal çatlaklar mevcut. Estetik onarım önerilir, yapısal risk düşük."
                )
                complexity < 0.50 -> Triple(
                    DamageType.TYPE_B,
                    String.format("%.1f mm", crackData.estimatedWidth),
                    "Belirgin çatlaklar tespit edildi. Uzman incelemesi ve onarım gerekli."
                )
                complexity < 0.75 -> Triple(
                    DamageType.TYPE_C,
                    String.format("%.1f mm", crackData.estimatedWidth),
                    "Ağır hasar! Beton kabuk dökülmesi mevcut. Acil müdahale gerekli."
                )
                else -> Triple(
                    DamageType.TYPE_D,
                    String.format("%.1f mm", crackData.estimatedWidth),
                    "ÇOK CİDDİ! Donatı burkulması ve çekirdek ezilmesi riski. Derhal tahliye edilmeli!"
                )
            }
            
            val crackLength = String.format("%.1f cm", crackData.estimatedLength)
            val totalArea = String.format("%.2f cm²", crackData.crackArea)
            val severity = (complexity * 100).coerceIn(0f, 100f)
            
            val prompt = """
                Deprem çatlak analizi yapıldı.
                
                Hasar Tipi: ${damageType.displayName}
                Çatlak Genişliği: $crackWidth
                Belirti: ${damageType.symptoms}
                Öneri: $recommendation
            """.trimIndent()
            
            // Direkt sonuç döndür (API'ye gerek yok, lokal analiz yeterli)
            return@withContext AnalysisResult.Success(
                damageType = damageType,
                crackWidth = crackWidth,
                crackLength = crackLength,
                totalArea = totalArea,
                severity = severity,
                description = damageType.symptoms,
                recommendation = recommendation
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext AnalysisResult.Error("Bağlantı Hatası: ${e.javaClass.simpleName} - ${e.message ?: "Bilinmeyen hata"}")
        }
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
}

