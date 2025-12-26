package com.example.indra.cropsuggestion


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// 1. Define the API Interface
interface CropApiService {
    @GET("api/gramin/crop-suggestions")
    suspend fun getSuggestions(
        @Query("location") location: String,
        @Query("soil_type") soilType: String,
        @Query("season") season: String,
        @Query("water_availability") waterAvailability: String,
        @Query("farm_size_acres") farmSize: Float,
        @Query("pincode") pincode: String? = null
    ): CropSuggestionResponse
}

// 2. Create the Singleton Client
object RetrofitClient {
    // ⚠️ REPLACE WITH YOUR ACTUAL IP
    private const val BASE_URL = "http://10.158.240.114:8000/"

    // 1. Create a custom OkHttpClient with increased timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Time to establish connection
        .readTimeout(60, TimeUnit.SECONDS)    // Time to wait for data from backend
        .writeTimeout(60, TimeUnit.SECONDS)   // Time to send data to backend
        .retryOnConnectionFailure(true)
        .build()

    val apiService: CropApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // 2. Attach the custom client to Retrofit
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CropApiService::class.java)
    }
}