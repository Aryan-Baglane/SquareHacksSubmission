package com.example.indra.weather

data class WeatherStats(
    val temp: Double,
    val rain: Double,
    val soilTemp: Double,
    val evapotranspiration: Double,
    val windSpeed: Double,

)

data class WeatherInsight(
    val rawSummary: String,
    val recommendation: String
)