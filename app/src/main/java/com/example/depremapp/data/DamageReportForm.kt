package com.example.depremapp.data

data class DamageReportForm(
    // İdari Bilgiler
    val il: String = "",
    val ilce: String = "",
    val belde: String = "",
    val mezra: String = "",
    val koy: String = "",
    val mahalle: String = "",
    
    // Nüfus ve Hane
    val nufus: String = "",
    val hane: String = "",
    
    // Afet Bilgileri
    val afetinTuru: String = "",
    val afetinTarihi: String = "",
    val sayfaNo: String = "",
    
    // Cadde/Sokak ve GPS
    val caddeSokak: String = "",
    val babaAdi: String = "",
    val yapiAdi: String = "",
    val tedasNo: String = "",
    val gpsKoordinat: String = "",
    
    // Yapı Kullanımı (İdari Bilgiler)
    val afetzedesiSoyadi: String = "",
    
    // Yapının Özellikleri (Evet/Hayır soruları)
    val mimariProje: Boolean? = null,
    val kacinciKat: String = "",
    val bodrum: Boolean? = null,
    val bodrum1: Boolean? = null,
    val zemin: Boolean? = null,
    val normal1: Boolean? = null,
    val normal2: Boolean? = null,
    val normal3: Boolean? = null,
    val catiKati: Boolean? = null,
    
    // Yapı Sistemi
    val yapidakiSistem: YapiSistemi = YapiSistemi.NONE,
    
    // Hasar Ait Bilgiler (K/T/A/D/S/M)
    val hasarDurumu: HasarDurumu = HasarDurumu.NONE,
    val tasiyiciSistem: TasiyiciSistem = TasiyiciSistem.NONE,
    val tasimaGucuKaybi: Boolean? = null,
    
    // Açıklamalar
    val aciklamalar: String = "",
    
    // İmza Bilgileri
    val adiSoyadi1: String = "",
    val meslegi1: String = "",
    val birimi1: String = "",
    
    val adiSoyadi2: String = "",
    val meslegi2: String = "",
    val birimi2: String = "",
    
    // Rapor tarihi
    val raporTarihi: String = ""
)

enum class YapiSistemi(val displayName: String) {
    NONE("Seçiniz"),
    YIGINMA("Yığınma"),
    KAGIR("Kâgir"),
    BETONARME("Betonarme"),
    CELIK("Çelik"),
    AHSAP("Ahşap"),
    PREFABRIK("Prefabrik"),
    DIGER("Diğer")
}

enum class HasarDurumu(val displayName: String, val description: String) {
    NONE("Seçiniz", ""),
    HASARSIZ("Hasarsız", "Yapıda hasar yok"),
    AZ("Az Hasarlı", "Hafif onarımla kullanılabilir"),
    ORTA("Orta Hasarlı", "Orta düzey onarım gerekli"),
    AGIR("Ağır Hasarlı", "Güçlendirme veya yıkım gerekli"),
    COK_AGIR("Çok Ağır Hasarlı", "Yıkım gerekli"),
    YIKILMIS("Yıkılmış", "Yapı tamamen yıkılmış")
}

enum class TasiyiciSistem(val displayName: String) {
    NONE("Seçiniz"),
    KOLONLAR("Kolonlar"),
    PERDELER("Perdeler"),
    KIRIŞLER("Kirişler"),
    DÖŞEMELER("Döşemeler"),
    DUVARLAR("Duvarlar"),
    MERDIVEN("Merdiven"),
    TEMEL("Temel")
}

data class FormStep(
    val title: String,
    val description: String,
    val fields: List<FormField>
)

sealed class FormField {
    data class TextInput(val label: String, val key: String, val placeholder: String = "") : FormField()
    data class NumberInput(val label: String, val key: String, val placeholder: String = "") : FormField()
    data class DateInput(val label: String, val key: String) : FormField()
    data class DropdownInput(val label: String, val key: String, val options: List<String>) : FormField()
    data class YesNoInput(val label: String, val key: String) : FormField()
    data class MultilineInput(val label: String, val key: String, val placeholder: String = "") : FormField()
}

