package com.example.indra.data

import java.util.UUID

data class Report(
    // Using a UUID for a truly unique and stable client-side ID
    val id: String = UUID.randomUUID().toString(),
    val location: String = "",
    val timestamp: Long = 0L,
    val feasibilityScore: Float = 0f,
    val annualHarvestingPotentialLiters: Long = 0L,
    val recommendedSolution: String = "",
    val estimatedCostInr: Int = 0,
    val name: String = "",
    val dwellers: Int = 0,
    val roofArea: Double = 0.0,
    val openSpace: Double = 0.0,
    // New fields for detailed assessment
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val roofType: String = "",
    val assessmentResponse: AssessmentResponse? = null
) {
    // Default constructor for Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        location = "",
        timestamp = 0L,
        feasibilityScore = 0f,
        annualHarvestingPotentialLiters = 0L,
        recommendedSolution = "",
        estimatedCostInr = 0,
        name = "",
        dwellers = 0,
        roofArea = 0.0,
        openSpace = 0.0,
        latitude = 0.0,
        longitude = 0.0,
        roofType = "",
        assessmentResponse = null
    )
}