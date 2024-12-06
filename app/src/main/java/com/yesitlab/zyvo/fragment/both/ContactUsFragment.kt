package com.yesitlab.zyvo.fragment.both

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentContactUsBinding
import com.yesitlab.zyvo.model.Location

class ContactUsFragment : Fragment() , OnMapReadyCallback {

    lateinit var binding :FragmentContactUsBinding
    private lateinit var googleMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentContactUsBinding.inflate(inflater, container, false)

        var mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(p0: GoogleMap) {

        googleMap = p0

        val locations = listOf(
            Location(37.7749, -122.4194, " $13/h"), Location(34.0522, -118.2437, " $15/h"),
            Location(40.7128, -74.0060, " $19/h"), Location(51.5074, -0.1278, " $23/h"),
            Location(48.8566, 2.3522, " $67/h")
        )

    }


}