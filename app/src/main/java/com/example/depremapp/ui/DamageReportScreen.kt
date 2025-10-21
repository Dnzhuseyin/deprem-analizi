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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
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
    val currentStep = viewModel.formSteps[formState.currentStep]
    val progress = (formState.currentStep + 1).toFloat() / viewModel.formSteps.size
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
            // Permission denied
            viewModel.clearLocationError()
        }
    }
    
    // Show location error if any
    formState.locationError?.let { error ->
        LaunchedEffect(error) {
            // Error will be shown in Snackbar
        }
    }
    
    if (formState.isComplete) {
        ReportSummaryScreen(
            formData = formState.formData,
            onConfirm = { onComplete(formState.formData) },
            onEdit = { viewModel.resetForm() }
        )
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Hasar Tespit Raporu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Adım ${formState.currentStep + 1} / ${viewModel.formSteps.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
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
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Step Title
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = currentStep.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentStep.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // GPS Location Button (only on first step - İdari Bilgiler)
                if (formState.currentStep == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📍 GPS ile Otomatik Doldur",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Konumunuzu kullanarak İl, İlçe, Mahalle ve koordinat bilgilerini otomatik doldurun",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
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
                                    Text("Konumumu Al")
                                }
                            }
                            
                            // Show error if any
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
                }
                
                // Form Fields
                currentStep.fields.forEach { field ->
                    when (field) {
                        is FormField.TextInput -> TextInputField(
                            label = field.label,
                            value = getFieldValue(formState.formData, field.key),
                            placeholder = field.placeholder,
                            onValueChange = { viewModel.updateField(field.key, it) }
                        )
                        is FormField.NumberInput -> NumberInputField(
                            label = field.label,
                            value = getFieldValue(formState.formData, field.key),
                            placeholder = field.placeholder,
                            onValueChange = { viewModel.updateField(field.key, it) }
                        )
                        is FormField.DateInput -> DateInputField(
                            label = field.label,
                            value = getFieldValue(formState.formData, field.key),
                            onValueChange = { viewModel.updateField(field.key, it) }
                        )
                        is FormField.DropdownInput -> DropdownInputField(
                            label = field.label,
                            value = getFieldValue(formState.formData, field.key),
                            options = field.options,
                            onValueChange = { viewModel.updateField(field.key, it) }
                        )
                        is FormField.YesNoInput -> YesNoInputField(
                            label = field.label,
                            value = getBooleanFieldValue(formState.formData, field.key),
                            onValueChange = { viewModel.updateField(field.key, it as Any) }
                        )
                        is FormField.MultilineInput -> MultilineInputField(
                            label = field.label,
                            value = getFieldValue(formState.formData, field.key),
                            placeholder = field.placeholder,
                            onValueChange = { viewModel.updateField(field.key, it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Navigation Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (formState.currentStep > 0) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Geri", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Geri")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    Button(
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (formState.currentStep < viewModel.formSteps.size - 1) "İleri" else "Tamamla")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (formState.currentStep < viewModel.formSteps.size - 1) Icons.Default.ArrowForward else Icons.Default.Check,
                            "İleri",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TextInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun NumberInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) onValueChange(it) },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
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
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
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

@Composable
fun YesNoInputField(
    label: String,
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        }
    }
}

@Composable
fun MultilineInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(12.dp),
        maxLines = 6
    )
}

@Composable
fun ReportSummaryScreen(
    formData: DamageReportForm,
    onConfirm: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓",
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rapor Tamamlandı",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bilgileri kontrol edin",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SummarySection("İdari Bilgiler", listOf(
                "İl" to formData.il,
                "İlçe" to formData.ilce,
                "Mahalle" to formData.mahalle
            ))
            
            SummarySection("Afet Bilgileri", listOf(
                "Afet Türü" to formData.afetinTuru,
                "Tarih" to formData.afetinTarihi
            ))
            
            SummarySection("Hasar Durumu", listOf(
                "Seviye" to formData.hasarDurumu.displayName,
                "Yapı Sistemi" to formData.yapidakiSistem.displayName
            ))
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Raporu Kaydet ve PDF Oluştur")
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Düzenle")
                }
            }
        }
    }
}

@Composable
fun SummarySection(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { (label, value) ->
                if (value.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

// Helper functions
private fun getFieldValue(form: DamageReportForm, key: String): String {
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
        "yapidakiSistem" -> form.yapidakiSistem.displayName
        "hasarDurumu" -> form.hasarDurumu.displayName
        "tasiyiciSistem" -> form.tasiyiciSistem.displayName
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

private fun getBooleanFieldValue(form: DamageReportForm, key: String): Boolean? {
    return when (key) {
        "mimariProje" -> form.mimariProje
        "bodrum" -> form.bodrum
        "bodrum1" -> form.bodrum1
        "zemin" -> form.zemin
        "normal1" -> form.normal1
        "normal2" -> form.normal2
        "normal3" -> form.normal3
        "catiKati" -> form.catiKati
        "tasimaGucuKaybi" -> form.tasimaGucuKaybi
        else -> null
    }
}

