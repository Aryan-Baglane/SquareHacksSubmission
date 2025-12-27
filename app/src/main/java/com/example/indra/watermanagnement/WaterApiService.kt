package com.example.indra.watermanagnement

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WaterApiService {
    // Matches the @app.get in main.py
    @GET("api/gramin/water-management/predict")
    suspend fun getWaterPlan(
        @Query("location") location: String,
        @Query("season") season: String,
        @Query("cattle_count") cattleCount: Int,
        @Query("household_members") members: Int,
        @Query("farm_size_acres") farmSize: Float,
        @Query("crop_type") cropType: String = "General",
        @Query("pincode") pincode: String? = null
    ): WaterManagementResponse
}

// Singleton (Use your existing RetrofitClient if you have one)
object WaterRetrofitClient {
    private const val BASE_URL = "http://10.158.240.174:8000/"

    // AI/GIS requests need a longer timeout (60 seconds)
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: WaterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Add this line
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WaterApiService::class.java)
    }
}