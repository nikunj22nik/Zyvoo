package com.yesitlab.zyvo.locationManager

import android.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.android.qualifiers.ApplicationContext

class LocationManager(var applicationContext : Context) {

    private lateinit var autocompleteTextView: AutoCompleteTextView
    private lateinit var placesClient: PlacesClient


    init {
        Places.initialize(applicationContext, "AIzaSyC9NuN_f-wESHh3kihTvpbvdrmKlTQurxw")
        placesClient = Places.createClient(applicationContext)
    }




        fun autoCompleteLocationWork(autocompleteTextView :AutoCompleteTextView){
            this.autocompleteTextView =autocompleteTextView
            autocompleteTextView.dropDownWidth = 350
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






}