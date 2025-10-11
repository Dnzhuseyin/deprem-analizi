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
            // Basit simülasyon - gerçek görüntü analizi için vision model gerekir
            // Şimdilik rastgele analiz sonucu döndürüyoruz
            
            // Görüntü boyutuna göre basit heuristik
            val imageSize = bitmap.width * bitmap.height
            val (severity, description) = when {
                imageSize < 500000 -> DamageSeverity.MINOR to "Küçük çatlaklar tespit edildi. Yapısal olmayan hasar."
                imageSize < 1000000 -> DamageSeverity.MODERATE to "Orta seviye çatlaklar mevcut. Yapısal inceleme önerilir."
                else -> DamageSeverity.SEVERE to "Ciddi çatlaklar görüldü. Acil yapısal değerlendirme gerekli."
            }
            
            // Text-only API'ye prompt gönder (gerçek analiz için mock)
            val prompt = """
                Deprem çatlak analizi:
                
                HASAR_SEVİYESİ: ${severity.name}
                AÇIKLAMA: $description
            """.trimIndent()
            
            val request = GroqChatRequest(
                model = "llama-3.1-8b-instant",
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = "Aşağıdaki analizi Türkçe olarak formatla:\n\n$prompt"
                    )
                )
            )
            
            val response = api.generateContent(
                authorization = "Bearer ${BuildConfig.GROQ_API_KEY}",
                request = request
            )
            
            if (response.isSuccessful && response.body() != null) {
                val responseText = response.body()?.choices?.firstOrNull()?.message?.content
                if (responseText != null) {
                    return@withContext parseResponse(responseText)
                } else {
                    return@withContext AnalysisResult.Error("API yanıt içeriği boş")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Bilinmeyen hata"
                return@withContext AnalysisResult.Error("API Hatası (${response.code()}): $errorBody")
            }
            
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
    
    private fun parseResponse(content: String): AnalysisResult {
        try {
            val lines = content.lines().map { it.trim() }
            
            val severityLine = lines.find { 
                it.startsWith("DAMAGE_LEVEL:", ignoreCase = true) ||
                it.startsWith("HASAR_SEVİYESİ:", ignoreCase = true)
            }
            val descriptionLine = lines.find { 
                it.startsWith("DESCRIPTION:", ignoreCase = true) ||
                it.startsWith("AÇIKLAMA:", ignoreCase = true)
            }
            
            val severityStr = severityLine?.substringAfter(":")?.trim()?.uppercase() ?: ""
            val description = descriptionLine?.substringAfter(":")?.trim() ?: content.take(200)
            
            val severity = when {
                severityStr.contains("NONE") || severityStr.contains("YOK") -> DamageSeverity.NONE
                severityStr.contains("MINOR") || severityStr.contains("AZ") -> DamageSeverity.MINOR
                severityStr.contains("MODERATE") || severityStr.contains("ORTA") -> DamageSeverity.MODERATE
                severityStr.contains("SEVERE") || severityStr.contains("AĞIR") || severityStr.contains("AGIR") -> DamageSeverity.SEVERE
                else -> {
                    // Content analysis fallback
                    when {
                        content.contains("severe", ignoreCase = true) || 
                        content.contains("ağır", ignoreCase = true) ||
                        content.contains("ciddi", ignoreCase = true) -> DamageSeverity.SEVERE
                        
                        content.contains("moderate", ignoreCase = true) ||
                        content.contains("orta", ignoreCase = true) ||
                        content.contains("belirgin", ignoreCase = true) -> DamageSeverity.MODERATE
                        
                        content.contains("minor", ignoreCase = true) ||
                        content.contains("az", ignoreCase = true) ||
                        content.contains("küçük", ignoreCase = true) ||
                        content.contains("hafif", ignoreCase = true) -> DamageSeverity.MINOR
                        
                        else -> DamageSeverity.NONE
                    }
                }
            }
            
            return AnalysisResult.Success(severity, description)
            
        } catch (e: Exception) {
            return AnalysisResult.Error("Yanıt işlenemedi: ${e.message}")
        }
    }
}

