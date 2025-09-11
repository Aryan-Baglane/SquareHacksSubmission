package com.example.indra.data

import com.google.gson.annotations.SerializedName

data class AssessmentRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("num_dwellers")
    val numDwellers: Int,
    @SerializedName("roof_area_sqm")
    val roofAreaSqm: Double,
    @SerializedName("open_space_sqm")
    val openSpaceSqm: Double,
    @SerializedName("roof_type")
    val roofType: String
)
