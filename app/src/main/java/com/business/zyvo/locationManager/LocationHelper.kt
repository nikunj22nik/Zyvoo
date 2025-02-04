package com.business.zyvo.locationManager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
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

        // Method to check if the location permission is granted
        fun checkLocationPermission(): Boolean {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        // Method to request location permission (for use in the Activity or Fragment)
        fun requestLocationPermission(activity: androidx.fragment.app.FragmentActivity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Method to handle the permission result (this should be called in your Activity or Fragment)
        fun handlePermissionResult(
            requestCode: Int,
            grantResults: IntArray,
            onPermissionGranted: () -> Unit
        ) {
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted()
                } else {
                    Toast.makeText(context, "Location permission is required to access your location.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Method to get the last known location in the background (asynchronously)
        fun getLocationInBackground(lifecycleOwner: Lifecycle, onLocationFetched: (Location?) -> Unit) {
            // Check if permission is granted
            if (!checkLocationPermission()) {
                Toast.makeText(context, "Permission required", Toast.LENGTH_SHORT).show()
                return
            }

            // Use lifecycleScope to handle coroutines
            CoroutineScope(Dispatchers.Main).launch {
                val location = fetchLocationInBackground()
                onLocationFetched(location)
            }
        }

        // Method to fetch the current location using FusedLocationProviderClient in the background


    private suspend fun fetchLocationInBackground(): Location? {
        return withContext(Dispatchers.IO) {
            try {
                // Check if location permission is granted
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Return null if permissions are not granted
                    return@withContext null
                }

                // Get the last known location from FusedLocationProviderClient
                val locationTask: Task<Location> = fusedLocationClient.lastLocation

                // Wait for the task to complete and get the result
                val location = locationTask.await()

                // Return the location if available
                location
            } catch (e: Exception) {
                // Handle any exceptions and return null
                null
            }
        }
    }

}