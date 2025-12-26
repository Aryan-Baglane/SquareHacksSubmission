package com.example.indra.cropsuggestion

import com.google.gson.annotations.SerializedName

data class CropSuggestionResponse(
    @SerializedName("recommendations") val recommendations: List<CropRecommendation>,
    @SerializedName("season_context") val seasonContext: String,
    @SerializedName("water_context") val waterContext: String,
    @SerializedName("general_advice") val generalAdvice: String
)

data class CropRecommendation(
    @SerializedName("crop_name") val cropName: String,
    @SerializedName("water_requirement_liters") val waterRequirementLiters: Double,
    @SerializedName("estimated_market_price_per_kg") val estimatedPrice: Double,
    @SerializedName("yield_per_acre_kg") val yieldPerAcre: Double,
    @SerializedName("total_profit_estimate") val profitEstimate: Double,
    @SerializedName("rank") val rank: Int,
    @SerializedName("justification") val justification: String,
    @SerializedName("environmental_impact_score") val envScore: Int,
    @SerializedName("farmer_ease_score") val easeScore: Int
)