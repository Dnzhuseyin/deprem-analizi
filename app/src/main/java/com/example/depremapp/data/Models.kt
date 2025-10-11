package com.example.depremapp.data

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

