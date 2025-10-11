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
    // Eşik değerleri düşürüldü - daha hassas tespit
    val estimatedLength = when {
        complexity < 0.05 -> (bitmap.height * 0.1f) / 10 // Çok hafif
        complexity < 0.15 -> (bitmap.height * 0.2f) / 10 
        complexity < 0.30 -> (bitmap.height * 0.4f) / 10 
        complexity < 0.50 -> (bitmap.height * 0.6f) / 10
        complexity < 0.75 -> (bitmap.height * 0.8f) / 10
        else -> (bitmap.height * 0.95f) / 10
    }
    
    // Çatlak genişliği tahmini (mm) - daha hassas aralıklar
    val estimatedWidth = when {
        complexity < 0.05 -> 0.2f
        complexity < 0.15 -> 0.4f
        complexity < 0.30 -> 1.2f
        complexity < 0.50 -> 2.5f
        complexity < 0.75 -> 6.0f
        else -> 12f
    }
    
    // Çatlak alanı (cm²)
    val crackArea = (estimatedLength * estimatedWidth) / 10
    
    return CrackAnalysisData(complexity, estimatedLength, estimatedWidth, crackArea)
}

fun analyzeImageComplexity(bitmap: Bitmap): Float {
    try {
        // Daha yüksek çözünürlük - daha iyi tespit
        val smallBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        
        var edgeCount = 0
        var strongEdgeCount = 0
        var totalPixels = 0
        var darkPixelCount = 0
        var crackLikePatternCount = 0
        
        for (y in 1 until smallBitmap.height - 1) {
            for (x in 1 until smallBitmap.width - 1) {
                val pixel = smallBitmap.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                
                // Karanlık piksel sayımı (çatlak olabilir) - eşik artırıldı
                if (brightness < 130) {
                    darkPixelCount++
                }
                
                // Çatlak benzeri desenler (çok koyu çizgiler)
                if (brightness < 80) {
                    crackLikePatternCount++
                }
                
                // Kenar tespiti (geliştirilmiş Sobel benzeri)
                val rightPixel = smallBitmap.getPixel(x + 1, y)
                val bottomPixel = smallBitmap.getPixel(x, y + 1)
                val diagPixel = smallBitmap.getPixel(x + 1, y + 1)
                
                val rightBrightness = (Color.red(rightPixel) + Color.green(rightPixel) + Color.blue(rightPixel)) / 3
                val bottomBrightness = (Color.red(bottomPixel) + Color.green(bottomPixel) + Color.blue(bottomPixel)) / 3
                val diagBrightness = (Color.red(diagPixel) + Color.green(diagPixel) + Color.blue(diagPixel)) / 3
                
                val horizontalGradient = abs(brightness - rightBrightness)
                val verticalGradient = abs(brightness - bottomBrightness)
                val diagonalGradient = abs(brightness - diagBrightness)
                
                val maxGradient = maxOf(horizontalGradient, verticalGradient, diagonalGradient)
                
                // Daha hassas kenar tespiti (eşik 20'ye düşürüldü)
                if (maxGradient > 20) {
                    edgeCount++
                }
                
                // Güçlü kenarlar (belirgin çatlaklar)
                if (maxGradient > 50) {
                    strongEdgeCount++
                }
                
                totalPixels++
            }
        }
        
        val edgeRatio = edgeCount.toFloat() / totalPixels
        val strongEdgeRatio = strongEdgeCount.toFloat() / totalPixels
        val darkRatio = darkPixelCount.toFloat() / totalPixels
        val crackPatternRatio = crackLikePatternCount.toFloat() / totalPixels
        
        // Geliştirilmiş karmaşıklık skoru
        val complexity = (
            edgeRatio * 0.4f +           // Normal kenarlar
            strongEdgeRatio * 0.3f +     // Güçlü kenarlar (çatlaklar)
            darkRatio * 0.2f +           // Karanlık alanlar
            crackPatternRatio * 0.1f     // Çatlak benzeri desenler
        ).coerceIn(0f, 1f)
        
        smallBitmap.recycle()
        
        // Minimum 0.05 complexity garantisi (tamamen düz yüzey dışında)
        return if (complexity < 0.02f) 0.05f else complexity
        
    } catch (e: Exception) {
        e.printStackTrace()
        return 0.15f // Varsayılan hafif seviye
    }
}

