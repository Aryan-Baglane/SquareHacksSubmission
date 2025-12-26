package com.example.indra.vendor

data class VendorResult(
    val name: String,
    val category: String,
    val location: String? = null,
    val contact: String? = null,
    val website: String? = null,
    val description: String? = null,
    val rating: Float? = null,
    val price_range: String? = null
)

data class VendorResponse(
    val success: Boolean,
    val location: String,
    val results: Map<String, List<VendorResult>>
)

data class DIYGuide(
    val title: String,
    val steps: List<String>,
    val materials_needed: List<String>,
    val difficulty: String,
    val estimated_time: String,
    val estimated_cost: String
)

data class DIYGuideResponse(
    val success: Boolean,
    val guide: DIYGuide,
    val tips: Map<String, List<String>>
)