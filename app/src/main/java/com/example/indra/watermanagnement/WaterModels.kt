package com.example.indra.watermanagnement

import androidx.compose.runtime.Immutable

/**
 * Marked as Immutable to allow Jetpack Compose to skip recomposition
 * if inputs haven't changed.
 */




import com.google.gson.annotations.SerializedName

data class WaterManagementResponse(
    @SerializedName("distribution") val distribution: WaterDistribution,
    @SerializedName("recommendations") val recommendations: List<String>,
    @SerializedName("ai_insights") val aiInsights: String,
    @SerializedName("water_status") val waterStatus: String,
    @SerializedName("gis_summary") val gisSummary: String
)

data class WaterDistribution(
    @SerializedName("irrigation_buckets") val irrigationBuckets: Int,
    @SerializedName("cattle_buckets") val cattleBuckets: Int,
    @SerializedName("drinking_buckets") val drinkingBuckets: Int,
    @SerializedName("irrigation_pct") val irrigationPct: Float,
    @SerializedName("cattle_pct") val cattlePct: Float,
    @SerializedName("drinking_pct") val drinkingPct: Float
)
sealed class WaterUiState {
    object Idle : WaterUiState()
    object Loading : WaterUiState()
    data class Success(val data: WaterManagementResponse) : WaterUiState()
    data class Error(val message: String) : WaterUiState()
}