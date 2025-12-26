package com.example.indra.screen.IndraGraminScreen.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.indra.BuildConfig
import com.example.indra.db.DatabaseProvider
import com.example.indra.weather.WeatherRepository
import com.example.indra.weather.WeatherStats
import kotlinx.coroutines.launch

class GraminDashboardViewModel : ViewModel() {
    private val TAG = "GraminDashboardVM"

    private val _dashboardState = MutableLiveData<DashboardState>(DashboardState.Loading)
    val dashboardState: LiveData<DashboardState> = _dashboardState

    private val db = DatabaseProvider.database()
    private val weatherRepository = WeatherRepository(apiKey = "AIzaSyAxzIjZDp733qV9CSeA_ucDmbLzmuDQ35w")


    fun loadDashboardData(uid: String, lat: Double?, lon: Double?, lang: String) {
        // Avoid reloading if data is already present
        if (_dashboardState.value is DashboardState.Success) return

        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                // Step 1: Fetch essential profile and weather data first
                val profile = db.getGraminProfile(uid) ?: throw Exception("Profile not found")
                val user = db.getUserProfile(uid)
                val (weather, aiSummary) = weatherRepository.getFarmingData(lat!!, lon!!, lang, profile.primaryCrop)


                // Step 2: Immediately update the UI with weather stats.
                // The AI summary will show a loading message for now.
                val intermediateState = DashboardState.Success(
                    userName = user?.name ?: "Farmer",
                    villageName = profile.village,
                    weatherStats = weather,
                    aiWeatherSummary = aiSummary
                )
                _dashboardState.value = intermediateState

            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                _dashboardState.value = DashboardState.Error("Error syncing dashboard: ${e.localizedMessage}")
            }
        }
    }
}