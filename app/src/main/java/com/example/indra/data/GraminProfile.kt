package com.example.indra.data

data class GraminProfile(
    val uid: String = "",
    val farmAreaAcres: Double = 0.0,
    val soilType: String = "", // e.g., Black, Alluvial, Sandy
    val irrigationSource: String = "", // e.g., Borewell, Canal, Rain-fed
    val village: String = "",
    val state: String = "", // CRITICAL for MSP and local climate
    val currentSeason: String = "", // e.g., Kharif, Rabi, Zaid
    val budgetRange: String = "", // e.g., Low, Medium, High
    val onboardingCompleted: Boolean = false,
    val primaryCrop: String = "",
    val language: String = "English"
)

data class CropAiDetails(
    val cropName: String = "",
    val msp: String = "",
    val waterLevel: String = "",
    val requirements: String = "",
    val revenue: String = "",
    val investment: String = "",
    val duration: String = ""
)
