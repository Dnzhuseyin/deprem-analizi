package com.example.depremapp.data

sealed class AnalysisResult {
    data class Success(
        val damageType: DamageType,
        val crackWidth: String,
        val crackLength: String,
        val totalArea: String,
        val severity: Float, // 0-100 arası
        val description: String,
        val recommendation: String
    ) : AnalysisResult()
    data class Error(val message: String) : AnalysisResult()
    object Loading : AnalysisResult()
}

enum class DamageType(
    val displayName: String,
    val widthRange: String,
    val symptoms: String,
    val colorHex: Long
) {
    TYPE_O(
        displayName = "O Tipi Hasar",
        widthRange = "Hasarsız",
        symptoms = "Hasarsız veya çok hafif, gözle görülür bir hasar yok",
        colorHex = 0xFF4CAF50  // Yeşil
    ),
    TYPE_A(
        displayName = "A Tipi Hasar",
        widthRange = "w ≤ 0.5 mm",
        symptoms = "Kılcal çatlakların olduğu hafif hasar seviyesi",
        colorHex = 0xFF8BC34A  // Açık Yeşil
    ),
    TYPE_B(
        displayName = "B Tipi Hasar",
        widthRange = "0.5 mm < w ≤ 3 mm",
        symptoms = "Belirgin çatlaklar ve kabuk ezilmesi başlamış",
        colorHex = 0xFFFFA726  // Turuncu
    ),
    TYPE_C(
        displayName = "C Tipi Hasar",
        widthRange = "Kabuk Atması",
        symptoms = "Betonun dış katmanının (kabuk) döküldüğü ağır hasar",
        colorHex = 0xFFFF7043  // Koyu Turuncu
    ),
    TYPE_D(
        displayName = "D Tipi Hasar",
        widthRange = "Kritik Hasar",
        symptoms = "Donatı burkulması, çekirdek ezilmesi - Çok ağır hasar/göçme riski",
        colorHex = 0xFFE53935  // Kırmızı
    )
}

