package com.example.depremapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.depremapp.data.AnalysisResult
import com.example.depremapp.data.DamageSeverity
import com.example.depremapp.ui.ImagePickerUtils
import com.example.depremapp.ui.theme.DepremAppTheme
import com.example.depremapp.viewmodel.CrackAnalysisViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: CrackAnalysisViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DepremAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrackAnalysisScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrackAnalysisScreen(viewModel: CrackAnalysisViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = ImagePickerUtils.uriToBitmap(context, it)
            bitmap?.let { bmp ->
                viewModel.onImageSelected(bmp)
            }
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.onImageSelected(it)
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            showPermissionDialog = true
        }
    }
    
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            showPermissionDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Deprem Çatlak Analizi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.selectedImage == null) {
                // Welcome Screen
                WelcomeScreen(
                    onCameraClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) -> {
                                cameraLauncher.launch(null)
                            }
                            else -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    onGalleryClick = {
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, permission) -> {
                                imagePickerLauncher.launch("image/*")
                            }
                            else -> {
                                storagePermissionLauncher.launch(permission)
                            }
                        }
                    }
                )
            } else {
                // Analysis Screen
                AnalysisScreen(
                    uiState = uiState,
                    onAnalyzeClick = { viewModel.analyzeImage() },
                    onResetClick = { viewModel.resetAnalysis() }
                )
            }
        }
    }
    
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("İzin Gerekli") },
            text = { Text("Bu özelliği kullanmak için gerekli izinleri vermeniz gerekmektedir.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Tamam")
                }
            }
        )
    }
}

@Composable
fun WelcomeScreen(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_info_details),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Deprem Çatlak Analizi",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Deprem çatlaklarını analiz ederek hasar seviyesini belirleyin",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Camera Button
        Button(
            onClick = onCameraClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_camera),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Fotoğraf Çek",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gallery Button
        OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Galeriden Seç",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AnalysisScreen(
    uiState: com.example.depremapp.viewmodel.UiState,
    onAnalyzeClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selected Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            uiState.selectedImage?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Seçilen Görüntü",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Analyze Button
        if (!uiState.isAnalyzing && uiState.analysisResult is AnalysisResult.Loading) {
            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Analiz Et",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Loading Indicator
        if (uiState.isAnalyzing) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Analiz ediliyor...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        // Results
        when (val result = uiState.analysisResult) {
            is AnalysisResult.Success -> {
                ResultCard(severity = result.severity, details = result.details)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onResetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Yeni Analiz",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is AnalysisResult.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hata",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.message,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onResetClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tekrar Dene")
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ResultCard(severity: DamageSeverity, details: String) {
    val (severityText, severityColor, severityEmoji) = when (severity) {
        DamageSeverity.NONE -> Triple("Hasar Tespit Edilmedi", Color(0xFF4CAF50), "✓")
        DamageSeverity.MINOR -> Triple("Az Hasarlı", Color(0xFFFFA726), "⚠")
        DamageSeverity.MODERATE -> Triple("Orta Hasarlı", Color(0xFFFF7043), "⚠⚠")
        DamageSeverity.SEVERE -> Triple("Ağır Hasarlı", Color(0xFFE53935), "⚠⚠⚠")
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = severityColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = severityEmoji,
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = severityText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = severityColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider(
                modifier = Modifier.fillMaxWidth(0.5f),
                color = severityColor.copy(alpha = 0.3f),
                thickness = 2.dp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = details,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
