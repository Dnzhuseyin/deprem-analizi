package com.example.depremapp.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.depremapp.data.*
import com.example.depremapp.viewmodel.DamageReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DamageReportScreen(
    viewModel: DamageReportViewModel,
    onComplete: (DamageReportForm) -> Unit,
    onBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current
    
    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                     permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchLocationAndFillForm(context)
        } else {
            viewModel.clearLocationError()
        }
    }
    
    if (formState.isComplete) {
        ReportSummaryScreen(
            formData = formState.formData,
            viewModel = viewModel,
            onConfirm = { onComplete(formState.formData) },
            onEdit = { viewModel.resetForm() }
        )
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Hasar Tespit Raporu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // GPS Auto-Fill Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📍 GPS ile Otomatik Doldur",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Konumunuzu kullanarak form bilgilerini otomatik doldurun",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        enabled = !formState.isLoadingLocation,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (formState.isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Konum Alınıyor...")
                        } else {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Konum",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Konumumu Al ve Formu Doldur")
                        }
                    }
                    
                    formState.locationError?.let { error ->
                        Text(
                            text = "⚠️ $error",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ALL FORM SECTIONS IN ONE PAGE
            
            // 1. İdari Bilgiler
            FormSectionCard(title = "1️⃣ İdari Bilgiler") {
                TextInputField("İl", getFieldValue(formState.formData, "il"), "Örn: İstanbul") {
                    viewModel.updateField("il", it)
                }
                TextInputField("İlçe", getFieldValue(formState.formData, "ilce"), "Örn: Kadıköy") {
                    viewModel.updateField("ilce", it)
                }
                TextInputField("Belde", getFieldValue(formState.formData, "belde")) {
                    viewModel.updateField("belde", it)
                }
                TextInputField("Mahalle", getFieldValue(formState.formData, "mahalle")) {
                    viewModel.updateField("mahalle", it)
                }
                TextInputField("Köy", getFieldValue(formState.formData, "koy")) {
                    viewModel.updateField("koy", it)
                }
                TextInputField("Mezra", getFieldValue(formState.formData, "mezra")) {
                    viewModel.updateField("mezra", it)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 2. Nüfus ve Hane Bilgileri
            FormSectionCard(title = "2️⃣ Nüfus ve Hane Bilgileri") {
                NumberInputField("Nüfus", getFieldValue(formState.formData, "nufus"), "Kişi sayısı") {
                    viewModel.updateField("nufus", it)
                }
                NumberInputField("Hane Sayısı", getFieldValue(formState.formData, "hane"), "Hane sayısı") {
                    viewModel.updateField("hane", it)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 3. Afet Bilgileri
            FormSectionCard(title = "3️⃣ Afet Bilgileri") {
                TextInputField("Afetin Türü", getFieldValue(formState.formData, "afetinTuru"), "Örn: Deprem") {
                    viewModel.updateField("afetinTuru", it)
                }
                DateInputField("Afetin Tarihi", getFieldValue(formState.formData, "afetinTarihi")) {
                    viewModel.updateField("afetinTarihi", it)
                }
                TextInputField("Sayfa No", getFieldValue(formState.formData, "sayfaNo")) {
                    viewModel.updateField("sayfaNo", it)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 4. Yapı Konum Bilgileri
            FormSectionCard(title = "4️⃣ Yapı Konum Bilgileri") {
                TextInputField("Cadde/Sokak", getFieldValue(formState.formData, "caddeSokak")) {
                    viewModel.updateField("caddeSokak", it)
                }
                TextInputField("Afetzede Soyadı", getFieldValue(formState.formData, "afetzedesiSoyadi")) {
                    viewModel.updateField("afetzedesiSoyadi", it)
                }
                TextInputField("Baba Adı", getFieldValue(formState.formData, "babaAdi")) {
                    viewModel.updateField("babaAdi", it)
                }
                TextInputField("Yapı Adı", getFieldValue(formState.formData, "yapiAdi")) {
                    viewModel.updateField("yapiAdi", it)
                }
                TextInputField("TEDAŞ No", getFieldValue(formState.formData, "tedasNo")) {
                    viewModel.updateField("tedasNo", it)
                }
                TextInputField("GPS Koordinat", getFieldValue(formState.formData, "gpsKoordinat"), "Enlem, Boylam") {
                    viewModel.updateField("gpsKoordinat", it)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 5. Yapı Özellikleri
            FormSectionCard(title = "5️⃣ Yapı Özellikleri") {
                YesNoInputField("Mimari Proje", formState.formData.mimariProje) {
                    viewModel.updateField("mimariProje", it as Any)
                }
                NumberInputField("Kaçıncı Kat", getFieldValue(formState.formData, "kacinciKat"), "Toplam kat sayısı") {
                    viewModel.updateField("kacinciKat", it)
                }
                YesNoInputField("Bodrum Kat", formState.formData.bodrum) {
                    viewModel.updateField("bodrum", it as Any)
                }
                YesNoInputField("Bodrum 1", formState.formData.bodrum1) {
                    viewModel.updateField("bodrum1", it as Any)
                }
                YesNoInputField("Zemin Kat", formState.formData.zemin) {
                    viewModel.updateField("zemin", it as Any)
                }
                YesNoInputField("1. Normal Kat", formState.formData.normal1) {
                    viewModel.updateField("normal1", it as Any)
                }
                YesNoInputField("2. Normal Kat", formState.formData.normal2) {
                    viewModel.updateField("normal2", it as Any)
                }
                YesNoInputField("3. Normal Kat", formState.formData.normal3) {
                    viewModel.updateField("normal3", it as Any)
                }
                YesNoInputField("Çatı Katı", formState.formData.catiKati) {
                    viewModel.updateField("catiKati", it as Any)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 6. Yapı Sistemi
            FormSectionCard(title = "6️⃣ Yapı Sistemi") {
                DropdownInputField(
                    "Yapıdaki Sistem",
                    formState.formData.yapidakiSistem.displayName,
                    YapiSistemi.values().map { it.displayName }
                ) {
                    viewModel.updateField("yapidakiSistem", it)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 7. Hasar Durumu
            FormSectionCard(title = "7️⃣ Hasar Durumu") {
                DropdownInputField(
                    "Hasar Seviyesi",
                    formState.formData.hasarDurumu.displayName,
                    HasarDurumu.values().map { it.displayName }
                ) {
                    viewModel.updateField("hasarDurumu", it)
                }
                DropdownInputField(
                    "Taşıyıcı Sistem Hasar",
                    formState.formData.tasiyiciSistem.displayName,
                    TasiyiciSistem.values().map { it.displayName }
                ) {
                    viewModel.updateField("tasiyiciSistem", it)
                }
                YesNoInputField("Taşıma Gücü Kaybı", formState.formData.tasimaGucuKaybi) {
                    viewModel.updateField("tasimaGucuKaybi", it as Any)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 8. Açıklamalar
            FormSectionCard(title = "8️⃣ Açıklamalar") {
                MultilineInputField("Açıklamalar", getFieldValue(formState.formData, "aciklamalar"), "Detaylı açıklama yazınız...") {
                    viewModel.updateField("aciklamalar", it)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 9. İmza Bilgileri
            FormSectionCard(title = "9️⃣ İmza Bilgileri") {
                TextInputField("Adı Soyadı 1", getFieldValue(formState.formData, "adiSoyadi1")) {
                    viewModel.updateField("adiSoyadi1", it)
                }
                TextInputField("Mesleği 1", getFieldValue(formState.formData, "meslegi1")) {
                    viewModel.updateField("meslegi1", it)
                }
                TextInputField("Birimi 1", getFieldValue(formState.formData, "birimi1")) {
                    viewModel.updateField("birimi1", it)
                }
                TextInputField("Adı Soyadı 2", getFieldValue(formState.formData, "adiSoyadi2")) {
                    viewModel.updateField("adiSoyadi2", it)
                }
                TextInputField("Mesleği 2", getFieldValue(formState.formData, "meslegi2")) {
                    viewModel.updateField("meslegi2", it)
                }
                TextInputField("Birimi 2", getFieldValue(formState.formData, "birimi2")) {
                    viewModel.updateField("birimi2", it)
                }
                DateInputField("Rapor Tarihi", getFieldValue(formState.formData, "raporTarihi")) {
                    viewModel.updateField("raporTarihi", it)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit Button
            Button(
                onClick = { 
                    viewModel.completeForm()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Raporu Tamamla", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

// Helper function to get field value as String
fun getFieldValue(form: DamageReportForm, key: String): String {
    return when (key) {
        "il" -> form.il
        "ilce" -> form.ilce
        "belde" -> form.belde
        "mahalle" -> form.mahalle
        "koy" -> form.koy
        "mezra" -> form.mezra
        "nufus" -> form.nufus
        "hane" -> form.hane
        "afetinTuru" -> form.afetinTuru
        "afetinTarihi" -> form.afetinTarihi
        "sayfaNo" -> form.sayfaNo
        "caddeSokak" -> form.caddeSokak
        "afetzedesiSoyadi" -> form.afetzedesiSoyadi
        "babaAdi" -> form.babaAdi
        "yapiAdi" -> form.yapiAdi
        "tedasNo" -> form.tedasNo
        "gpsKoordinat" -> form.gpsKoordinat
        "kacinciKat" -> form.kacinciKat
        "aciklamalar" -> form.aciklamalar
        "adiSoyadi1" -> form.adiSoyadi1
        "meslegi1" -> form.meslegi1
        "birimi1" -> form.birimi1
        "adiSoyadi2" -> form.adiSoyadi2
        "meslegi2" -> form.meslegi2
        "birimi2" -> form.birimi2
        "raporTarihi" -> form.raporTarihi
        else -> ""
    }
}

// Input Field Components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputField(
    label: String,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberInputField(
    label: String,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) onValueChange(it) },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("GG/AA/YYYY") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true
    )
}

@Composable
fun YesNoInputField(
    label: String,
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = value == true,
                onClick = { onValueChange(true) },
                label = { Text("Evet") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = value == false,
                onClick = { onValueChange(false) },
                label = { Text("Hayır") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = value == null,
                onClick = { onValueChange(null) },
                label = { Text("Bilinmiyor") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownInputField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultilineInputField(
    label: String,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(bottom = 12.dp),
        maxLines = 5
    )
}

@Composable
fun ReportSummaryScreen(
    formData: DamageReportForm,
    viewModel: DamageReportViewModel,
    onConfirm: () -> Unit,
    onEdit: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current
    
    // Show success message when PDF is created
    LaunchedEffect(formState.pdfFile) {
        formState.pdfFile?.let { file ->
            // PDF created successfully - can show snackbar or toast
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "✅ Rapor Tamamlandı",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Hasar tespit raporu başarıyla dolduruldu",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Show summary
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📋 Rapor Özeti", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                SummaryItem("İl", formData.il)
                SummaryItem("İlçe", formData.ilce)
                SummaryItem("Mahalle", formData.mahalle)
                SummaryItem("GPS", formData.gpsKoordinat)
                SummaryItem("Afet Türü", formData.afetinTuru)
                SummaryItem("Hasar Durumu", formData.hasarDurumu.displayName)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // PDF Status Card
        if (formState.pdfFile != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "PDF Oluşturuldu",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formState.pdfFile!!.name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "Konum: ${formState.pdfFile!!.parent}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // PDF Error
        formState.pdfError?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    "⚠️ $error",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Generate PDF Button
        Button(
            onClick = { viewModel.generateAndSavePdf(context) },
            enabled = !formState.isGeneratingPdf,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            if (formState.isGeneratingPdf) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onTertiary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("PDF Oluşturuluyor...")
            } else {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PDF Olarak Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Onayla ve Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Düzenle")
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "$label: ",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(120.dp)
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
