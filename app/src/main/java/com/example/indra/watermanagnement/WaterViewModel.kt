package com.example.indra.watermanagnement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable

/**
 * Marks the UI state as Immutable.
 * This allows the Compose compiler to skip recomposition if the state
 * reference hasn't changed, improving performance.
 */


class WaterViewModel : ViewModel() {

    // Internal mutable state flow
    private val _uiState = MutableStateFlow<WaterUiState>(WaterUiState.Idle)

    /**
     * Expose as an immutable StateFlow.
     * Using asStateFlow() prevents external classes from modifying the state,
     * ensuring a single source of truth.
     */
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    /**
     * Fetches the water management plan based on farm parameters.
     * Uses viewModelScope for automatic cancellation when the user navigates
     * away.
     */
    fun getWaterPlan(
        location: String,
        season: String,
        cattle: String,
        members: String,
        acres: String
    ) {
        viewModelScope.launch {
            _uiState.value = WaterUiState.Loading

            try {
                // Input Sanitization
                val cattleCount = cattle.toIntOrNull() ?: 0
                val familyMembers = members.toIntOrNull() ?: 4
                val farmSize = acres.toFloatOrNull() ?: 2.0f

                val response = WaterRetrofitClient.apiService.getWaterPlan(
                    location = location,
                    season = season,
                    cattleCount = cattleCount,
                    members = familyMembers,
                    farmSize = farmSize
                )

                _uiState.value = WaterUiState.Success(response)
            } catch (e: Exception) {
                // Log the stack trace internally for debugging
                e.printStackTrace()
                _uiState.value = WaterUiState.Error(
                    e.localizedMessage ?: "An unexpected error occurred. Please check your connection."
                )
            }
        }
    }

    /**
     * Resets the UI state to allow the user to input fresh data.
     */
    fun reset() {
        _uiState.value = WaterUiState.Idle
    }
}