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

class GroqRepository {
    
    private val api = GeminiRetrofitInstance.api
    
    suspend fun analyzeImage(bitmap: Bitmap): AnalysisResult = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Analyze this image for earthquake damage cracks. Respond ONLY in this format:
                
                DAMAGE_LEVEL: [MINOR/MODERATE/SEVERE/NONE]
                DESCRIPTION: [Brief description in Turkish]
                
                Criteria:
                - MINOR: Small surface cracks, non-structural damage
                - MODERATE: Significant cracks, potential structural concern
                - SEVERE: Large cracks, serious structural damage indicators
                - NONE: No cracks or not related to earthquake damage
                
                Please respond exactly in this format.
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
            
            // Try Groq-style API call
            val response = api.generateContent(
                model = "llama-3.2-90b-vision-preview",
                apiKey = BuildConfig.GROQ_API_KEY,
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

