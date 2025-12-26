package com.example.indra.vendor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface VendorApiService {
    @GET("api/vendors/search")
    suspend fun searchVendors(
        @Query("location") location: String,
        @Query("search_type") searchType: String = "all"
    ): VendorResponse

    @GET("api/vendors/diy-guide")
    suspend fun getDiyGuide(): DIYGuideResponse
}

object VendorRetrofitClient {
    private const val BASE_URL = "http://10.158.240.114:8000/"

    val apiService: VendorApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VendorApiService::class.java)
    }
}