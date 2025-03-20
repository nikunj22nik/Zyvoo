package com.business.zyvo.fragment.both

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.DialogFragment.STYLE_NO_TITLE
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentMapDialogBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapDialogFragment : DialogFragment(), OnMapReadyCallback {

    var latitude :String ="0.00"
    var longitude :String ="0.00"
    private lateinit var binding: FragmentMapDialogBinding
    private lateinit var googleMap: GoogleMap
    private val zoomLevel = 15f
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            latitude = it.getString("latitude").toString()
            longitude = it.getString("longitude").toString()
        }

        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapDialogBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        val location = LatLng(latitude.toDouble(), longitude.toDouble())
        googleMap.addMarker(MarkerOptions().position(location).title("Marker at $latitude, $longitude"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
    }

    // Lifecycle management for MapView
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }



}