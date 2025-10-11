package com.example.depremapp.data

import android.graphics.Bitmap
import android.util.Base64
import com.example.depremapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

sealed class AnalysisResult {
    data class Success(val severity: DamageSeverity, val details: String) : AnalysisResult()
    data class Error(val message: String) : AnalysisResult()
    object Loading : AnalysisResult()
}

enum class DamageSeverity {
    NONE,
    MINOR,      // Az Hasarlı
    MODERATE,   // Orta Hasarlı
    SEVERE      // Ağır Hasarlı
}

class GeminiRepository {
    
    private val api = GeminiRetrofitInstance.api
    
    suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Bu görüntüyü deprem çatlak analizi açısından incele ve SADECE aşağıdaki formatı kullanarak cevap ver:
                
                HASAR_SEVİYESİ: [AZ/ORTA/AĞIR/YOK]
                AÇIKLAMA: [Kısa açıklama]
                
                Değerlendirme kriterleri:
                - AZ: Küçük yüzeysel çatlaklar, yapısal olmayan hasar
                - ORTA: Belirgin çatlaklar, yapısal endişe potansiyeli
                - AĞIR: Büyük çatlaklar, ciddi yapısal hasar göstergeleri
                - YOK: Çatlak yok veya deprem hasarı ile ilgili değil
                
                Lütfen kesinlikle bu formatta yanıt ver.
            """.trimIndent()
            
            // Convert bitmap to base64
            val base64Image = bitmapToBase64(bitmap)
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(
                                inlineData = InlineData(
                                    mimeType = "image/jpeg",
                                    data = base64Image
                                )
                            )
                        )
                    )
                )
            )
            
            val response = api.generateContent(
                model = "gemini-pro-vision",
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
            
            if (response.isSuccessful && response.body() != null) {
                val responseText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
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
            
            val severityLine = lines.find { it.startsWith("HASAR_SEVİYESİ:", ignoreCase = true) }
            val descriptionLine = lines.find { it.startsWith("AÇIKLAMA:", ignoreCase = true) }
            
            val severityStr = severityLine?.substringAfter(":")?.trim()?.uppercase() ?: ""
            val description = descriptionLine?.substringAfter(":")?.trim() ?: content.take(200)
            
            val severity = when {
                severityStr.contains("YOK") || severityStr.contains("NONE") -> DamageSeverity.NONE
                severityStr.contains("AZ") || severityStr.contains("MINOR") -> DamageSeverity.MINOR
                severityStr.contains("ORTA") || severityStr.contains("MODERATE") -> DamageSeverity.MODERATE
                severityStr.contains("AĞIR") || severityStr.contains("SEVERE") || severityStr.contains("AGIR") -> DamageSeverity.SEVERE
                else -> {
                    // Format uymazsa içerik analizi
                    when {
                        content.contains("ağır", ignoreCase = true) || 
                        content.contains("ciddi", ignoreCase = true) ||
                        content.contains("severe", ignoreCase = true) -> DamageSeverity.SEVERE
                        
                        content.contains("orta", ignoreCase = true) ||
                        content.contains("belirgin", ignoreCase = true) ||
                        content.contains("moderate", ignoreCase = true) -> DamageSeverity.MODERATE
                        
                        content.contains("az", ignoreCase = true) ||
                        content.contains("küçük", ignoreCase = true) ||
                        content.contains("minor", ignoreCase = true) ||
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

