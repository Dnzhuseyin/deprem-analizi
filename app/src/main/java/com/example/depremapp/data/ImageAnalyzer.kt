package com.example.depremapp.data

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.sqrt

data class CrackAnalysisData(
    val complexity: Float,
    val estimatedLength: Float, // cm cinsinden
    val estimatedWidth: Float,  // mm cinsinden
    val crackArea: Float // cm² cinsinden
)

fun analyzeCrackDetails(bitmap: Bitmap): CrackAnalysisData {
    val complexity = analyzeImageComplexity(bitmap)
    
    // Çatlak uzunluğu tahmini (görüntü yüksekliğine oranla)
    val estimatedLength = when {
        complexity < 0.15 -> 0f
        complexity < 0.30 -> (bitmap.height * 0.3f) / 10 // piksel -> cm
        complexity < 0.50 -> (bitmap.height * 0.5f) / 10
        complexity < 0.75 -> (bitmap.height * 0.7f) / 10
        else -> (bitmap.height * 0.9f) / 10
    }
    
    // Çatlak genişliği tahmini (mm)
    val estimatedWidth = when {
        complexity < 0.15 -> 0f
        complexity < 0.30 -> 0.3f
        complexity < 0.50 -> 1.5f
        complexity < 0.75 -> 5f
        else -> 10f
    }
    
    // Çatlak alanı (cm²)
    val crackArea = (estimatedLength * estimatedWidth) / 10
    
    return CrackAnalysisData(complexity, estimatedLength, estimatedWidth, crackArea)
}

fun analyzeImageComplexity(bitmap: Bitmap): Float {
    try {
        // Görüntüyü küçült (performans için)
        val smallBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
        
        var edgeCount = 0
        var totalPixels = 0
        var darkPixelCount = 0
        
        for (y in 1 until smallBitmap.height - 1) {
            for (x in 1 until smallBitmap.width - 1) {
                val pixel = smallBitmap.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                
                // Karanlık piksel sayımı (çatlak olabilir)
                if (brightness < 100) {
                    darkPixelCount++
                }
                
                // Kenar tespiti (basit gradyan)
                val rightPixel = smallBitmap.getPixel(x + 1, y)
                val bottomPixel = smallBitmap.getPixel(x, y + 1)
                
                val rightBrightness = (Color.red(rightPixel) + Color.green(rightPixel) + Color.blue(rightPixel)) / 3
                val bottomBrightness = (Color.red(bottomPixel) + Color.green(bottomPixel) + Color.blue(bottomPixel)) / 3
                
                val horizontalGradient = abs(brightness - rightBrightness)
                val verticalGradient = abs(brightness - bottomBrightness)
                
                // Kenar varsa (güçlü gradyan)
                if (horizontalGradient > 30 || verticalGradient > 30) {
                    edgeCount++
                }
                
                totalPixels++
            }
        }
        
        val edgeRatio = edgeCount.toFloat() / totalPixels
        val darkRatio = darkPixelCount.toFloat() / totalPixels
        
        // Karmaşıklık skoru: kenar yoğunluğu + karanlık alan oranı
        val complexity = (edgeRatio * 0.7f + darkRatio * 0.3f).coerceIn(0f, 1f)
        
        smallBitmap.recycle()
        
        return complexity
        
    } catch (e: Exception) {
        e.printStackTrace()
        return 0.3f // Varsayılan orta seviye
    }
}

