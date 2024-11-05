package com.yesitlab.zyvo.activity.guest

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.AdapterAddOn
import com.yesitlab.zyvo.adapter.guest.AdapterReview
import com.yesitlab.zyvo.databinding.ActivityRestaurantDetailBinding


class RestaurantDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding :ActivityRestaurantDetailBinding
    lateinit var adapterAddon :AdapterAddOn
    lateinit var adapterReview: AdapterReview
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityRestaurantDetailBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initialization()
    }


    fun initialization(){
        adapterAddon = AdapterAddOn(this,getAddOnList().subList(0,4))
        adapterReview = AdapterReview(this, mutableListOf())
        binding.tvReadMoreLess.setCollapsedText("Read More")
        binding.tvReadMoreLess.setCollapsedTextColor(com.yesitlab.zyvo.R.color.green_color_bar)
        binding.recyclerAddOn.adapter = adapterAddon
        binding.recyclerAddOn.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recyclerReviews.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recyclerReviews.adapter = adapterReview
        val textView = binding.tvShowMore
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvLocationName.paintFlags = binding.tvLocationName.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvShowMore.setOnClickListener {
          binding.tvShowMore.visibility = View.GONE
          adapterAddon.updateAdapter(getAddOnList())
        }
        clickListeners()
    }

    private fun clickListeners(){
        binding.showMoreReview.setOnClickListener {
            adapterReview.updateAdapter(7)
        }
    }

     private fun  getAddOnList() :  MutableList<String>{
        var list = mutableListOf<String>()
        list.add("Computer Screen")
        list.add("Bed Sheets")
        list.add("Phone charger")
        list.add("Ring Light")
        list.add("Left Light")
        list.add("Water Bottle")
        return list
    }



    private fun shareText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()  // Important to call in onResume
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Important to call in onPause
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()  // Important to call in onDestroy
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()  // Important to call in onLowMemory
    }

    override fun onMapReady(p0: GoogleMap) {

        mMap = p0
        val newYork = LatLng(40.7128, -74.0060)
        mMap?.addMarker(MarkerOptions().position(newYork).title("Marker in New York"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 10f))

    }


}