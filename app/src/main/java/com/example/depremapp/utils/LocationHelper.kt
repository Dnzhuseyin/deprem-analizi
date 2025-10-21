package com.example.depremapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val il: String = "",
    val ilce: String = "",
    val mahalle: String = "",
    val fullAddress: String = ""
)

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder = Geocoder(context, Locale("tr", "TR"))

    /**
     * Checks if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Gets current location with high accuracy
     */
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location: Location? ->
                    continuation.resume(location)
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Gets location data with address information
     */
    suspend fun getLocationWithAddress(): LocationData? {
        val location = getCurrentLocation() ?: return null
        
        val latitude = location.latitude
        val longitude = location.longitude
        
        // Get address from coordinates (Reverse Geocoding)
        val addresses = getAddressFromLocation(latitude, longitude)
        
        return if (addresses.isNotEmpty()) {
            val address = addresses[0]
            LocationData(
                latitude = latitude,
                longitude = longitude,
                il = address.adminArea ?: "", // İl
                ilce = address.subAdminArea ?: "", // İlçe
                mahalle = address.locality ?: address.subLocality ?: "", // Mahalle
                fullAddress = address.getAddressLine(0) ?: ""
            )
        } else {
            LocationData(
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    /**
     * Reverse Geocoding: Coordinates to Address
     */
    @Suppress("DEPRECATION")
    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): List<Address> {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        continuation.resume(addresses)
                    }
                } else {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    continuation.resume(addresses ?: emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                continuation.resume(emptyList())
            }
        }
    }
}

