package com.example.depremapp.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.depremapp.data.AnalysisResult
import com.example.depremapp.data.GroqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val selectedImage: Bitmap? = null,
    val analysisResult: AnalysisResult = AnalysisResult.Loading,
    val isAnalyzing: Boolean = false
)

class CrackAnalysisViewModel : ViewModel() {
    
    private val repository = GroqRepository()
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun onImageSelected(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            selectedImage = bitmap,
            analysisResult = AnalysisResult.Loading,
            isAnalyzing = false
        )
    }
    
    fun analyzeImage() {
        val bitmap = _uiState.value.selectedImage ?: return
        
        _uiState.value = _uiState.value.copy(isAnalyzing = true)
        
        viewModelScope.launch {
            val result = repository.analyzeImage(bitmap)
            _uiState.value = _uiState.value.copy(
                analysisResult = result,
                isAnalyzing = false
            )
        }
    }
    
    fun resetAnalysis() {
        _uiState.value = UiState()
    }
}

