package com.example.indra.vendor



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow


sealed class VendorUiState {
    object Idle : VendorUiState()
    object Loading : VendorUiState()
    data class Success(
        val vendors: Map<String, List<VendorResult>>,
        val location: String
    ) : VendorUiState()
    data class Error(val message: String) : VendorUiState()
}

class VendorViewModel : ViewModel() {

    // UI State for the results list
    private val _uiState = MutableStateFlow<VendorUiState>(VendorUiState.Idle)
    val uiState: StateFlow<VendorUiState> = _uiState.asStateFlow()

    // Persistent state for the search text field
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Updates the search query string as the user types
     */
    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /**
     * Executes the search using the current searchQuery value
     */
    fun performSearch() {
        val location = _searchQuery.value.trim()
        if (location.isBlank()) return

        viewModelScope.launch {
            _uiState.value = VendorUiState.Loading
            try {
                val response = VendorRetrofitClient.apiService.searchVendors(
                    location = location
                )

                if (response.success) {
                    _uiState.value = VendorUiState.Success(
                        vendors = response.results,
                        location = response.location
                    )
                } else {
                    _uiState.value = VendorUiState.Error("No vendors found for '$location'")
                }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error("Check your connection or IP address.")
            }
        }
    }

    /**
     * Clears text and results
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = VendorUiState.Idle
    }
}