package com.business.zyvo.locationManager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocationHelper(private val context: Context) {
        private val fusedLocationClient: FusedLocationProviderClient by lazy {
            LocationServices.getFusedLocationProviderClient(context)
        }

        private val LOCATION_PERMISSION_REQUEST_CODE = 1000
        fun checkLocationPermission(): Boolean {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        fun getLocationInBackground(lifecycleOwner: Lifecycle, onLocationFetched: (Location?) -> Unit) {
            if (!checkLocationPermission()) {
                Toast.makeText(context, "Permission required", Toast.LENGTH_SHORT).show()
                return
            }

            CoroutineScope(Dispatchers.Main).launch {
                val location = fetchLocationInBackground()
                onLocationFetched(location)
            }
        }

        private suspend fun fetchLocationInBackground(): Location? {
        return withContext(Dispatchers.IO) {
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return@withContext null
                }

                val locationTask: Task<Location> = fusedLocationClient.lastLocation
                val location = locationTask.await()
                location
            } catch (e: Exception) {
                null
            }
        }
    }

}