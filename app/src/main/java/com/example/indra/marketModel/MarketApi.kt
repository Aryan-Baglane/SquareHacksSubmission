package com.example.indra.marketModel



import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1. Define Endpoints
interface MarketApi {

    @GET("/api/market/states")
    suspend fun getStates(): List<String>

    @GET("/api/market/districts")
    suspend fun getDistricts(@Query("state") state: String): List<String>

    @GET("/api/market/crops")
    suspend fun getCrops(
        @Query("state") state: String,
        @Query("district") district: String
    ): List<String>

    @GET("/api/market/data")
    suspend fun getMarketData(
        @Query("state") state: String?,
        @Query("district") district: String?,
        @Query("crop") crop: String?,
        @Query("from") from: String?, // YYYY-MM-DD
        @Query("to") to: String?      // YYYY-MM-DD
    ): List<MarketRecord>
}

// 2. Singleton Object
object RetrofitInstance {
    // IP 10.0.2.2 connects to your laptop's localhost from Emulator
    private const val BASE_URL = "http://10.158.240.114:8080/"

    val api: MarketApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketApi::class.java)
    }
}