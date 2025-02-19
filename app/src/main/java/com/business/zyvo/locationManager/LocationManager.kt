package com.business.zyvo.locationManager

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.business.zyvo.model.AddressDetails
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale


class LocationManager(var applicationContext : Context,var applicationActivity : AppCompatActivity? =null) {

    private lateinit var autocompleteTextView: AutoCompleteTextView
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private var placesClient: PlacesClient
    private val geocoder by lazy { Geocoder(applicationContext, Locale.getDefault()) }

    init {
        Places.initialize(applicationContext, "AIzaSyC9NuN_f-wESHh3kihTvpbvdrmKlTQurxw")
        placesClient = Places.createClient(applicationContext)

    }

    @SuppressLint("ClickableViewAccessibility")
    fun autoCompleteLocationWork(autocompleteTextView :AutoCompleteTextView){
            this.autocompleteTextView =autocompleteTextView

            autocompleteTextView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val width: Int = autocompleteTextView.getMeasuredWidth()
            Log.d("TESTING_WIDTH", width.toString())
           // autocompleteTextView.dropDownWidth = width
            autocompleteTextView.threshold = 1 // Start suggesting after 1 character

            autocompleteTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let {
                        if (it.isNotEmpty()) {
                            fetchAutocompleteSuggestions(it.toString(),applicationContext)
                            autocompleteTextView.showDropDown()
                        }else{
                            autocompleteTextView.dismissDropDown()
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

        autocompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedLocation = parent.getItemAtPosition(position) as String

            fetchPlaceDetails(selectedLocation) { latitude, longitude ->
                selectedLatitude = latitude
                selectedLongitude = longitude

                Log.d("Location", "Lat: $latitude, Lng: $longitude")
            }
        }


        autocompleteTextView.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (autocompleteTextView.text.isNotEmpty()) {
                        autocompleteTextView.showDropDown()
                    }
                }
            }

            autocompleteTextView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick() // Ensure accessibility
                    if (autocompleteTextView.text.isNotEmpty()) {
                        autocompleteTextView.showDropDown()
                    }
                }
                false
            }

        }

    private fun fetchAutocompleteSuggestions(query: String,context:Context) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                val suggestions = response.autocompletePredictions.map { it.getPrimaryText(null).toString() }
                Log.d("TESTING_ZYVOO_LOCATION","Suggestions For Location :- "+suggestions.size.toString())
                val adapter = ArrayAdapter(context, R.layout.simple_dropdown_item_1line, suggestions)
                autocompleteTextView.setAdapter(adapter)
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }

    fun getAddressFromCoordinates(latitude: Double, longitude: Double): AddressDetails {
        // Use Geocoder to get address details
        val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

        return if (addressList != null && addressList.isNotEmpty()) {
            val address = addressList[0]
            val city = address.locality
            val postalCode = address.postalCode
            val state = address.adminArea
            val country = address.countryName

            // Return the structured data
            AddressDetails(city, postalCode, state, country)
        } else {
            // Return null values if address not found
            AddressDetails("", "", "", "")
        }
    }

    private fun fetchPlaceDetails(placeName: String, callback: (Double, Double) -> Unit) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(placeName)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                if (response.autocompletePredictions.isNotEmpty()) {
                    val placeId = response.autocompletePredictions[0].placeId

                    val placeRequest = com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
                    val fetchRequest = com.google.android.libraries.places.api.net.FetchPlaceRequest.builder(placeId, listOf(placeRequest)).build()

                    placesClient.fetchPlace(fetchRequest)
                        .addOnSuccessListener { placeResponse ->
                            placeResponse.place.latLng?.let { latLng ->
                                callback(latLng.latitude, latLng.longitude)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FetchPlaceError", "Error fetching place details: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PredictionError", "Error fetching predictions: ${exception.message}")
            }
    }



    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)

    // Define a callback interface to return the location data
    interface LocationCallback {
        fun onLocationFetched(latitude: Double, longitude: Double)
        fun onLocationError(error: String)
    }

    private var locationCallback: LocationCallback? = null

    // Permission request launcher
    private val requestPermissionLauncher =
        applicationActivity?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                // Permission granted, fetch location
                fetchLocationInBackground()
            } else {
                // Handle permission denial, show a message or fallback
                locationCallback?.onLocationError("Location permissions denied")
            }
        }

    // Request permissions at runtime
    fun checkAndRequestPermissions(callback: LocationCallback) {
        locationCallback = callback

        val permissionsNeeded = mutableListOf<String>()

        // Check if permission is granted for foreground location
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Check if permission is granted for background location (Android 10 and above)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher?.launch(permissionsNeeded.toTypedArray())
        } else {
            // Permissions already granted, fetch location
            fetchLocationInBackground()
        }
    }

    // Fetch the current location in a background thread
    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchLocationInBackground() {
        // Use Kotlin Coroutines to run the task in the background thread
        GlobalScope.launch(Dispatchers.Main) {
            val location = withContext(Dispatchers.IO) {
                getCurrentLocation()
            }

            // Now that location is fetched, invoke the callback
            location?.let {
                // Callback with latitude and longitude
                locationCallback?.onLocationFetched(it.latitude, it.longitude)
            } ?: run {
                // Handle the case where location is null (e.g., location not available)
                locationCallback?.onLocationError("Unable to fetch location")
            }
        }
    }

    // Fetch the current location using FusedLocationProviderClient
    suspend fun getCurrentLocation(): android.location.Location? {
        return if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationTask: Task<android.location.Location> = fusedLocationClient.lastLocation

            try {
                val location = locationTask.await() // Suspend until we get the location
                location
            } catch (e: Exception) {
                // Handle any errors (e.g., task failed, no location available)
                null
            }
        } else {
            // Permissions not granted, return null
            null
        }
    }



}