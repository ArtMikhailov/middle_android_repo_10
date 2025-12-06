package ru.yandex.buggyweatherapp.repository

import android.app.Application
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import ru.yandex.buggyweatherapp.model.Location
import java.util.Locale

/*
1. Заменил Context на Application, чтобы внутрь мог попасть только Application context,
чтобы предотвратить возможную утечку памяти, связанную с передачей конекста Activity.

2. Устранил утечку памяти через сallback передаваемый в метод getCurrentLocation.

3. Добавил метод cancelLocationUpdates для остановки получения обновлений локации
когда это больше не нужно.
 */
class LocationRepository(
    private val application: Application
) {
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(application)
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private var locationListener: ((Location?) -> Unit)? = null

    fun addLocationListener(listener: (Location?) -> Unit) {
        locationListener = listener
    }

    fun removeLocationListener() {
        locationListener = null
    }

    fun getCurrentLocation() {
        try {
            cancelLocationUpdates()
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = Location(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            name = getCityNameFromCoordinates(location.latitude, location.longitude)
                        )
                        currentLocation = userLocation
                        locationListener?.invoke(userLocation)
                    } else {
                        requestUpdatesTillFirstNonNullLocation()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationRepository", "Error getting location", e)
                    locationListener?.invoke(null)
                }
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            locationListener?.invoke(null)
        }
    }
    
    
    private fun requestUpdatesTillFirstNonNullLocation() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .build()
            
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val userLocation = Location(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            name = getCityNameFromCoordinates(location.latitude, location.longitude)
                        )
                        currentLocation = userLocation
                        locationListener?.invoke(userLocation)
                        cancelLocationUpdates()
                    }
                }
            }.also {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    it,
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            locationListener?.invoke(null)
        }
    }

    private fun getCityNameFromCoordinates(latitude: Double, longitude: Double): String? {
        try {
            
            val geocoder = Geocoder(application, Locale.getDefault())
            
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            return if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                if (address.locality != null) {
                    address.locality
                } else if (address.subAdminArea != null) {
                    address.subAdminArea
                } else {
                    address.adminArea
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting city name", e)
            return null
        }
    }

    fun cancelLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
    }
}