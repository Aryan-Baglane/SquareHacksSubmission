package com.example.indra.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import java.util.Locale

class LocationRepository {

    @SuppressLint("MissingPermission")
    fun fetchLocation(
        context: Context,
        fusedClient: FusedLocationProviderClient,
        callback: (LocationData?) -> Unit
    ) {
        fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback(LocationData(location.toAddress(context), location.latitude, location.longitude))
            } else {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setMaxUpdates(1).build()
                fusedClient.requestLocationUpdates(request, object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val loc = result.lastLocation
                        callback(loc?.let { LocationData(it.toAddress(context), it.latitude, it.longitude) })
                        fusedClient.removeLocationUpdates(this)
                    }
                }, null)
            }
        }.addOnFailureListener { callback(null) }
    }

    private fun Location.toAddress(context: Context): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) "${addresses[0].locality}, ${addresses[0].adminArea}" else null
        } catch (e: Exception) {
            e.printStackTrace(); null
        }
    }
}
