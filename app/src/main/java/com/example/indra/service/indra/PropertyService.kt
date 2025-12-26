package com.example.indra.service.indra

import com.example.indra.auth.AuthApi
import com.example.indra.data.AssessmentInput
import com.example.indra.data.FeasibilityCalculator
import com.example.indra.data.Property
import com.example.indra.db.DatabaseProvider
import kotlinx.datetime.Clock
import kotlin.random.Random

object PropertyService {

    suspend fun getUserProperties(): List<Property> {
        val user = AuthApi.currentUser() ?: return emptyList()
        return DatabaseProvider.database().getUserProperties(user.uid)
    }

    suspend fun addPropertyFromAssessment(
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        roofArea: Double,
        openSpace: Double,
        numberOfDwellers: Int,
        roofType: String = "Concrete"
    ): Property {
        val user = AuthApi.currentUser() ?: throw IllegalStateException("User not authenticated")

        // Create assessment input using the CORRECT parameter names
        val assessmentInput = AssessmentInput(
            location = address,
            name = name, // Added this parameter
            dwellers = numberOfDwellers, // Renamed from numberOfDwellers to dwellers to match AssessmentInput
            roofArea = roofArea, // Renamed from roofAreaSqMeters
            openSpace = openSpace, // Renamed from openSpaceSqMeters
            roofType = roofType
        )

        // Calculate feasibility. The FeasibilityCalculator.calculate function expects
        // an AssessmentInput object that contains all the necessary data.
        val report = FeasibilityCalculator.calculate(assessmentInput)

        // Create property
        val property = Property(
            id = "PROP-${Random.Default.nextInt(1000, 9999)}",
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            feasibilityScore = report.feasibilityScore,
            annualHarvestingPotentialLiters = report.annualHarvestingPotentialLiters,
            recommendedSolution = report.recommendedSolution,
            estimatedCostInr = report.estimatedCostInr,
            lastAssessmentDate = Clock.System.now().toEpochMilliseconds(),
            propertyType = "Residential",
            // Use the correct parameter names for the Property data class
            roofArea = roofArea,
            openSpace = openSpace
        )

        // Save to database
        DatabaseProvider.database().addProperty(user.uid, property)

        return property
    }

    suspend fun updateProperty(property: Property) {
        val user = AuthApi.currentUser() ?: throw IllegalStateException("User not authenticated")
        DatabaseProvider.database().updateProperty(user.uid, property)
    }

    suspend fun deleteProperty(propertyId: String) {
        val user = AuthApi.currentUser() ?: throw IllegalStateException("User not authenticated")
        DatabaseProvider.database().deleteProperty(user.uid, propertyId)
    }

    suspend fun reassessProperty(property: Property): Property {
        val user = AuthApi.currentUser() ?: throw IllegalStateException("User not authenticated")

        // Create assessment input from existing property data
        val assessmentInput = AssessmentInput(
            location = property.address,
            name = property.name, // Added this parameter
            dwellers = property.dwellers, // Added this parameter
            roofArea = property.roofArea, // Renamed from roofAreaSqMeters
            openSpace = property.openSpace, // Renamed from openSpaceSqMeters
            roofType = "Concrete"
        )

        // Calculate new feasibility
        val report = FeasibilityCalculator.calculate(assessmentInput)

        // Update property with new assessment
        val updatedProperty = property.copy(
            feasibilityScore = report.feasibilityScore,
            annualHarvestingPotentialLiters = report.annualHarvestingPotentialLiters,
            recommendedSolution = report.recommendedSolution,
            estimatedCostInr = report.estimatedCostInr,
            lastAssessmentDate = Clock.System.now().toEpochMilliseconds()
        )

        // Save to database
        DatabaseProvider.database().updateProperty(user.uid, updatedProperty)

        return updatedProperty
    }
}