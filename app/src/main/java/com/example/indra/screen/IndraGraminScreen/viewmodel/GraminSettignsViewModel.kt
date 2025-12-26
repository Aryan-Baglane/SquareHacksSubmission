package com.example.indra.screen.IndraGraminScreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.indra.auth.AuthApi
import com.example.indra.data.CombinedUserProfile
import com.example.indra.data.GraminProfile
import com.example.indra.db.DatabaseProvider
import kotlinx.coroutines.launch

class GraminSettingsViewModel : ViewModel() {

    private val dbApi = DatabaseProvider.database()
    private val authApi = AuthApi // Get an instance of the AuthApi

    private val _combinedProfile = MutableLiveData<CombinedUserProfile?>(null)
    val combinedProfile: LiveData<CombinedUserProfile?> = _combinedProfile

    private val _updateStatus = MutableLiveData<Pair<Boolean, String>>()
    val updateStatus: LiveData<Pair<Boolean, String>> = _updateStatus

    init {
        fetchData()
    }

    private fun fetchData() {
        // Launch a coroutine to handle suspend functions
        viewModelScope.launch {
            try {
                // CORRECT: Call suspend function from within a coroutine
                val user = authApi.currentUser() ?: return@launch

                val userProfile = dbApi.getUserProfile(user.uid)?.copy(
                    photoUrl = user.photoUrl?.toString()
                )
                val graminProfile = dbApi.getGraminProfile(user.uid)

                if (userProfile != null && graminProfile != null) {
                    _combinedProfile.postValue(CombinedUserProfile(userProfile, graminProfile))
                }
            } catch (e: Exception) {
                _combinedProfile.postValue(null)
            }
        }
    }

    fun updateGraminProfile(
        village: String,
        farmArea: Double,
        soilType: String,
        irrigation: String,
        language: String,
        primaryCrop: String
    ) {
        // Launch a coroutine for the update process
        viewModelScope.launch {
            try {
                // CORRECT: Call suspend function from within a coroutine
                val user = authApi.currentUser() ?: return@launch

                val currentGraminProfile = _combinedProfile.value?.graminProfile ?: return@launch

                val updatedProfile = currentGraminProfile.copy(
                    uid = user.uid,
                    village = village,
                    farmAreaAcres = farmArea,
                    soilType = soilType,
                    irrigationSource = irrigation,
                    language = language,
                    primaryCrop = primaryCrop
                )

                dbApi.setGraminProfile(updatedProfile)
                _updateStatus.postValue(Pair(true, "Profile updated successfully!"))
                // Refresh data to show the latest changes
                fetchData()
            } catch (e: Exception) {
                _updateStatus.postValue(Pair(false, "Failed to update profile: ${e.message}"))
            }
        }
    }
}
