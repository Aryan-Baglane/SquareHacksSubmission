package com.example.indra.marketModel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketViewModel : ViewModel() {

    // --- State Variables ---
    private val _records = MutableStateFlow<List<MarketRecord>>(emptyList())
    val records = _records.asStateFlow()

    private val _states = MutableStateFlow<List<String>>(emptyList())
    val states = _states.asStateFlow()

    private val _districts = MutableStateFlow<List<String>>(emptyList())
    val districts = _districts.asStateFlow()

    private val _crops = MutableStateFlow<List<String>>(emptyList())
    val crops = _crops.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchStates() // Load states when app starts
    }

    // --- API Calls ---

    fun fetchStates() {
        viewModelScope.launch {
            try {
                _states.value = RetrofitInstance.api.getStates()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchDistricts(state: String) {
        viewModelScope.launch {
            try {
                _districts.value = RetrofitInstance.api.getDistricts(state)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun fetchCrops(state: String, district: String) {
        viewModelScope.launch {
            try {
                _crops.value = RetrofitInstance.api.getCrops(state, district)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun searchMarketData(
        state: String?, district: String?, crop: String?,
        from: String?, to: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // If "All" is selected, treat it as null
                val cleanState = if (state == "Select State") null else state
                val cleanDistrict = if (district == "Select District") null else district
                val cleanCrop = if (crop == "Select Crop") null else crop

                _records.value = RetrofitInstance.api.getMarketData(
                    cleanState, cleanDistrict, cleanCrop, from, to
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _records.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}