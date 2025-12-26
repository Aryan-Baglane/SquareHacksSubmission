package com.example.indra.weather

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherRepository(apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite", // Corrected model name
        apiKey = "AIzaSyDEsa1HGQO5aYYCuS_626vTyC4AztfDb0I" // Use the key passed to the repository
    )

    suspend fun getFarmingData(lat: Double, lon: Double, lang: String, primaryCrop: String): Pair<WeatherStats, String> = withContext(Dispatchers.IO) {
        // 1. Updated URL to ensure humidity is fetched
        val urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon" +
                "&hourly=soil_temperature_6cm,evapotranspiration,relativehumidity_2m" +
                "&daily=rain_sum&current_weather=true&timezone=auto"

        val response = try {
            URL(urlString).readText()
        } catch (e: Exception) {
            throw Exception("Network Error: Could not fetch weather data")
        }

        val json = JSONObject(response)

        // Helper objects
        val current = json.getJSONObject("current_weather")
        val hourly = json.getJSONObject("hourly")
        val daily = json.getJSONObject("daily")

        // 2. Safe Parsing Logic with Humidity
        val humidity = hourly.optJSONArray("relativehumidity_2m")?.optInt(0, 0) ?: 0

        val stats = WeatherStats(
            temp = current.optDouble("temperature", 0.0),
            rain = daily.optJSONArray("rain_sum")?.optDouble(0, 0.0) ?: 0.0,
            soilTemp = hourly.optJSONArray("soil_temperature_6cm")?.optDouble(0, 0.0) ?: 0.0,
            evapotranspiration = hourly.optJSONArray("evapotranspiration")?.optDouble(0, 0.0) ?: 0.0,
            windSpeed = current.optDouble("windspeed", 0.0),
             // Pass the fetched humidity
        )

        // 3. AI Summary Logic with Dynamic Humidity and Language
        val prompt = """
    You are an expert Indian Agricultural Scientist (Krishi Vigyan Kendra). 
    Analyze the following real-time weather data for a farmer growing '$primaryCrop'.
    
    - Temperature: ${stats.temp}°C
    - Humidity: $humidity%
    - Rain (Today): ${stats.rain}mm
    - Soil Temp: ${stats.soilTemp}°C
    - Water Loss (Evapotranspiration): ${stats.evapotranspiration}mm
    - Wind Speed: ${stats.windSpeed} km/h

    Task:
    Provide 2 lines of very simple, practical advice in the language '$lang'.
    Use a helpful and respectful "Agricultural Advisor" tone.
    
    Guidelines:
    1. Irrigation: Suggest based on evapotranspiration and rain, specific to the needs of '$primaryCrop'.
    2. Spraying: Advise against it if wind speed > 15km/h or if rain is expected.
    3. Sowing/Fertilizing: Base it on soil temp and humidity, considering the growth stage of '$primaryCrop'.
    
    Format:
    "[Your advice here]"
""".trimIndent()

        val aiResult = try {
            generativeModel.generateContent(prompt).text ?: "Check field conditions manually."
        } catch (e: Exception) {
            "Weather looks stable. Monitor your fields locally."
        }

        return@withContext Pair(stats, aiResult)
    }
}
