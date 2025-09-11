package com.example.indra.network



import com.example.indra.data.AssessmentRequest
import com.example.indra.data.AssessmentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AssessmentApiService {

    @POST("assess")
    suspend fun assessRwhPotential(@Body request: AssessmentRequest): Response<AssessmentResponse>

}