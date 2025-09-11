package com.example.indra.data

import com.google.gson.annotations.SerializedName

data class AssessmentResponse(
    @SerializedName("location_info")
    val locationInfo: LocationInfo = LocationInfo(),
    @SerializedName("feasibility_score")
    val feasibilityScore: Double = 0.0,
    @SerializedName("feasibility_insights")
    val feasibilityInsights: String = "",
    @SerializedName("rwh_analysis")
    val rwhAnalysis: RWHAnalysis = RWHAnalysis(),
    @SerializedName("ar_analysis")
    val arAnalysis: ARAnalysis = ARAnalysis(),
    @SerializedName("cost_benefit_analysis")
    val costBenefitAnalysis: CostBenefitAnalysis = CostBenefitAnalysis()
)

data class LocationInfo(
    @SerializedName("avg_annual_rainfall_mm")
    val avgAnnualRainfallMm: Double = 0.0,
    @SerializedName("principal_aquifer")
    val principalAquifer: String = "",
    @SerializedName("soil_type")
    val soilType: String = "",
    @SerializedName("soil_permeability")
    val soilPermeability: String = "",
    @SerializedName("predicted_groundwater_depth_mbgl")
    val predictedGroundwaterDepthMbgl: Double = 0.0
)

data class RWHAnalysis(
    @SerializedName("potential_annual_runoff_liters")
    val potentialAnnualRunoffLiters: Double = 0.0,
    @SerializedName("recommended_tank_size_liters")
    val recommendedTankSizeLiters: Int = 0,
    @SerializedName("notes")
    val notes: String = ""
)

data class ARAnalysis(
    @SerializedName("is_feasible")
    val isFeasible: Boolean = false,
    @SerializedName("recommended_structure_type")
    val recommendedStructureType: String = "",
    @SerializedName("structure_dimensions")
    val structureDimensions: Map<String, String> = emptyMap(),
    @SerializedName("notes")
    val notes: String = ""
)

data class CostBenefitAnalysis(
    @SerializedName("estimated_initial_investment")
    val estimatedInitialInvestment: Double = 0.0,
    @SerializedName("annual_operating_maintenance_cost")
    val annualOperatingMaintenanceCost: Double = 0.0,
    @SerializedName("annual_water_savings_liters")
    val annualWaterSavingsLiters: Double = 0.0,
    @SerializedName("annual_monetary_savings")
    val annualMonetarySavings: Double = 0.0,
    @SerializedName("payback_period_years")
    val paybackPeriodYears: Double = 0.0
)
