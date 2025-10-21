package com.example.depremapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.depremapp.data.*
import com.example.depremapp.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportFormState(
    val currentStep: Int = 0,
    val formData: DamageReportForm = DamageReportForm(),
    val isComplete: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null
)

class DamageReportViewModel : ViewModel() {
    
    private val _formState = MutableStateFlow(ReportFormState())
    val formState: StateFlow<ReportFormState> = _formState.asStateFlow()
    
    val formSteps = listOf(
        FormStep(
            title = "İdari Bilgiler",
            description = "Yapının bulunduğu konum bilgilerini girin",
            fields = listOf(
                FormField.TextInput("İl", "il", "Örn: İstanbul"),
                FormField.TextInput("İlçe", "ilce", "Örn: Kadıköy"),
                FormField.TextInput("Belde", "belde"),
                FormField.TextInput("Mahalle", "mahalle"),
                FormField.TextInput("Köy", "koy"),
                FormField.TextInput("Mezra", "mezra")
            )
        ),
        FormStep(
            title = "Nüfus ve Hane Bilgileri",
            description = "Etkilenen nüfus bilgileri",
            fields = listOf(
                FormField.NumberInput("Nüfus", "nufus", "Kişi sayısı"),
                FormField.NumberInput("Hane Sayısı", "hane", "Hane sayısı")
            )
        ),
        FormStep(
            title = "Afet Bilgileri",
            description = "Afet detayları",
            fields = listOf(
                FormField.TextInput("Afetin Türü", "afetinTuru", "Örn: Deprem"),
                FormField.DateInput("Afetin Tarihi", "afetinTarihi"),
                FormField.TextInput("Sayfa No", "sayfaNo")
            )
        ),
        FormStep(
            title = "Yapı Konum Bilgileri",
            description = "Yapının detaylı konum bilgileri",
            fields = listOf(
                FormField.TextInput("Cadde/Sokak", "caddeSokak"),
                FormField.TextInput("Afetzede Soyadı", "afetzedesiSoyadi"),
                FormField.TextInput("Baba Adı", "babaAdi"),
                FormField.TextInput("Yapı Adı", "yapiAdi"),
                FormField.TextInput("TEDAŞ No", "tedasNo"),
                FormField.TextInput("GPS Koordinat (Varsa)", "gpsKoordinat", "Enlem, Boylam")
            )
        ),
        FormStep(
            title = "Yapı Özellikleri",
            description = "Yapının fiziksel özellikleri",
            fields = listOf(
                FormField.YesNoInput("Mimari Proje", "mimariProje"),
                FormField.NumberInput("Kaçıncı Kat", "kacinciKat", "Toplam kat sayısı"),
                FormField.YesNoInput("Bodrum Kat", "bodrum"),
                FormField.YesNoInput("Bodrum 1", "bodrum1"),
                FormField.YesNoInput("Zemin Kat", "zemin"),
                FormField.YesNoInput("1. Normal Kat", "normal1"),
                FormField.YesNoInput("2. Normal Kat", "normal2"),
                FormField.YesNoInput("3. Normal Kat", "normal3"),
                FormField.YesNoInput("Çatı Katı", "catiKati")
            )
        ),
        FormStep(
            title = "Yapı Sistemi",
            description = "Yapının taşıyıcı sistem bilgisi",
            fields = listOf(
                FormField.DropdownInput(
                    "Yapıdaki Sistem",
                    "yapidakiSistem",
                    YapiSistemi.values().map { it.displayName }
                )
            )
        ),
        FormStep(
            title = "Hasar Durumu",
            description = "Yapının hasar durumu değerlendirmesi",
            fields = listOf(
                FormField.DropdownInput(
                    "Hasar Seviyesi",
                    "hasarDurumu",
                    HasarDurumu.values().map { it.displayName }
                ),
                FormField.DropdownInput(
                    "Taşıyıcı Sistem Hasar",
                    "tasiyiciSistem",
                    TasiyiciSistem.values().map { it.displayName }
                ),
                FormField.YesNoInput("Taşıma Gücü Kaybı", "tasimaGucuKaybi")
            )
        ),
        FormStep(
            title = "Açıklamalar",
            description = "Ek bilgiler ve gözlemler",
            fields = listOf(
                FormField.MultilineInput("Açıklamalar", "aciklamalar", "Detaylı açıklama yazınız...")
            )
        ),
        FormStep(
            title = "İmza Bilgileri",
            description = "Raporu hazırlayanların bilgileri",
            fields = listOf(
                FormField.TextInput("Adı Soyadı 1", "adiSoyadi1"),
                FormField.TextInput("Mesleği 1", "meslegi1"),
                FormField.TextInput("Birimi 1", "birimi1"),
                FormField.TextInput("Adı Soyadı 2", "adiSoyadi2"),
                FormField.TextInput("Mesleği 2", "meslegi2"),
                FormField.TextInput("Birimi 2", "birimi2"),
                FormField.DateInput("Rapor Tarihi", "raporTarihi")
            )
        )
    )
    
    fun updateField(key: String, value: Any) {
        val currentForm = _formState.value.formData
        val updatedForm = when (key) {
            "il" -> currentForm.copy(il = value as String)
            "ilce" -> currentForm.copy(ilce = value as String)
            "belde" -> currentForm.copy(belde = value as String)
            "mahalle" -> currentForm.copy(mahalle = value as String)
            "koy" -> currentForm.copy(koy = value as String)
            "mezra" -> currentForm.copy(mezra = value as String)
            "nufus" -> currentForm.copy(nufus = value as String)
            "hane" -> currentForm.copy(hane = value as String)
            "afetinTuru" -> currentForm.copy(afetinTuru = value as String)
            "afetinTarihi" -> currentForm.copy(afetinTarihi = value as String)
            "sayfaNo" -> currentForm.copy(sayfaNo = value as String)
            "caddeSokak" -> currentForm.copy(caddeSokak = value as String)
            "afetzedesiSoyadi" -> currentForm.copy(afetzedesiSoyadi = value as String)
            "babaAdi" -> currentForm.copy(babaAdi = value as String)
            "yapiAdi" -> currentForm.copy(yapiAdi = value as String)
            "tedasNo" -> currentForm.copy(tedasNo = value as String)
            "gpsKoordinat" -> currentForm.copy(gpsKoordinat = value as String)
            "mimariProje" -> currentForm.copy(mimariProje = value as Boolean?)
            "kacinciKat" -> currentForm.copy(kacinciKat = value as String)
            "bodrum" -> currentForm.copy(bodrum = value as Boolean?)
            "bodrum1" -> currentForm.copy(bodrum1 = value as Boolean?)
            "zemin" -> currentForm.copy(zemin = value as Boolean?)
            "normal1" -> currentForm.copy(normal1 = value as Boolean?)
            "normal2" -> currentForm.copy(normal2 = value as Boolean?)
            "normal3" -> currentForm.copy(normal3 = value as Boolean?)
            "catiKati" -> currentForm.copy(catiKati = value as Boolean?)
            "yapidakiSistem" -> currentForm.copy(yapidakiSistem = YapiSistemi.values().first { it.displayName == value })
            "hasarDurumu" -> currentForm.copy(hasarDurumu = HasarDurumu.values().first { it.displayName == value })
            "tasiyiciSistem" -> currentForm.copy(tasiyiciSistem = TasiyiciSistem.values().first { it.displayName == value })
            "tasimaGucuKaybi" -> currentForm.copy(tasimaGucuKaybi = value as Boolean?)
            "aciklamalar" -> currentForm.copy(aciklamalar = value as String)
            "adiSoyadi1" -> currentForm.copy(adiSoyadi1 = value as String)
            "meslegi1" -> currentForm.copy(meslegi1 = value as String)
            "birimi1" -> currentForm.copy(birimi1 = value as String)
            "adiSoyadi2" -> currentForm.copy(adiSoyadi2 = value as String)
            "meslegi2" -> currentForm.copy(meslegi2 = value as String)
            "birimi2" -> currentForm.copy(birimi2 = value as String)
            "raporTarihi" -> currentForm.copy(raporTarihi = value as String)
            else -> currentForm
        }
        
        _formState.value = _formState.value.copy(formData = updatedForm)
    }
    
    fun nextStep() {
        val currentStep = _formState.value.currentStep
        if (currentStep < formSteps.size - 1) {
            _formState.value = _formState.value.copy(currentStep = currentStep + 1)
        } else {
            _formState.value = _formState.value.copy(isComplete = true)
        }
    }
    
    fun previousStep() {
        val currentStep = _formState.value.currentStep
        if (currentStep > 0) {
            _formState.value = _formState.value.copy(currentStep = currentStep - 1)
        }
    }
    
    fun resetForm() {
        _formState.value = ReportFormState()
    }
    
    /**
     * Fetches current location and auto-fills location fields
     */
    fun fetchLocationAndFillForm(context: Context) {
        val locationHelper = LocationHelper(context)
        
        // Check permission
        if (!locationHelper.hasLocationPermission()) {
            _formState.value = _formState.value.copy(
                locationError = "Konum izni verilmedi"
            )
            return
        }
        
        // Start loading
        _formState.value = _formState.value.copy(
            isLoadingLocation = true,
            locationError = null
        )
        
        viewModelScope.launch {
            try {
                val locationData = locationHelper.getLocationWithAddress()
                
                if (locationData != null) {
                    // Auto-fill location fields
                    updateField("il", locationData.il)
                    updateField("ilce", locationData.ilce)
                    updateField("mahalle", locationData.mahalle)
                    updateField("gpsKoordinat", 
                        "${String.format("%.6f", locationData.latitude)}, ${String.format("%.6f", locationData.longitude)}")
                    
                    _formState.value = _formState.value.copy(
                        isLoadingLocation = false,
                        locationError = null
                    )
                } else {
                    _formState.value = _formState.value.copy(
                        isLoadingLocation = false,
                        locationError = "Konum alınamadı"
                    )
                }
            } catch (e: SecurityException) {
                _formState.value = _formState.value.copy(
                    isLoadingLocation = false,
                    locationError = "Konum izni gerekli"
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isLoadingLocation = false,
                    locationError = "Konum alınamadı: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clears location error message
     */
    fun clearLocationError() {
        _formState.value = _formState.value.copy(locationError = null)
    }
}

