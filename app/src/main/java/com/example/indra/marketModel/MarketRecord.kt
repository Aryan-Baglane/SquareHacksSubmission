package com.example.indra.marketModel



data class MarketRecord(
    val id: String? = null,
    val state: String,
    val district: String,
    val market: String,
    val commodity: String,
    val variety: String,
    val minPrice: String,
    val maxPrice: String,
    val modalPrice: String,
    val date: String // YYYY-MM-DD
)