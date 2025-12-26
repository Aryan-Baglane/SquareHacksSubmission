package com.example.indra.screen.IndraGraminScreen.viewmodel



import com.example.indra.data.CropAiDetails
import com.example.indra.weather.WeatherStats

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val userName: String,
        val villageName: String,
        val weatherStats: WeatherStats,
        val aiWeatherSummary: String
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}