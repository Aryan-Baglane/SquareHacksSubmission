// In a new file, e.g., 'AssessmentRepository.kt'
// You might need to adjust the package name

package com.example.indra.data

import com.example.indra.network.AssessmentApiService
import com.example.indra.network.NetworkModule

// Interface to define the contract for the repository
interface AssessmentRepository {
    suspend fun performAssessment(request: AssessmentRequest): Result<AssessmentResponse>
}

// Implementation of the repository that uses the Retrofit service
class NetworkAssessmentRepository(
    private val apiService: AssessmentApiService
) : AssessmentRepository {
    override suspend fun performAssessment(request: AssessmentRequest): Result<AssessmentResponse> {
        return try {
            val response = apiService.assessRwhPotential(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Assessment failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// This will provide the concrete implementation to your UI
object AssessmentRepositoryProvider {
    fun repository(): AssessmentRepository {
        return NetworkAssessmentRepository(NetworkModule.assessmentApiService)
    }
}