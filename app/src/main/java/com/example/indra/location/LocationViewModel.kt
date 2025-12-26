package com.example.indra.location

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient

class LocationViewModel : ViewModel() {

    private val _locationData = MutableLiveData<LocationData?>()
    val locationData: LiveData<LocationData?> = _locationData

    private val repository = LocationRepository()

    fun fetchLocation(context: Context, fusedClient: FusedLocationProviderClient) {
        repository.fetchLocation(context, fusedClient) { location ->
            _locationData.postValue(location)
        }
    }
}
