package com.example.indra.cropsuggestion



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define UI States
sealed class CropUiState {
    object Idle : CropUiState()
    object Loading : CropUiState()
    data class Success(val data: CropSuggestionResponse) : CropUiState()
    data class Error(val message: String) : CropUiState()
}

class CropViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CropUiState>(CropUiState.Idle)
    val uiState: StateFlow<CropUiState> = _uiState

    fun fetchSuggestions(
        location: String,
        soilType: String,
        season: String,
        water: String,
        acres: Float,
        pincode: String
    ) {
        viewModelScope.launch {
            _uiState.value = CropUiState.Loading
            try {
                val response = RetrofitClient.apiService.getSuggestions(
                    location = location,
                    soilType = soilType,
                    season = season,
                    waterAvailability = water,
                    farmSize = acres,
                    pincode = pincode
                )
                _uiState.value = CropUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = CropUiState.Error("Failed to connect: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = CropUiState.Idle
    }
}