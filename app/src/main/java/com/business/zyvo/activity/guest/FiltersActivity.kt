package com.business.zyvo.activity.guest

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.github.mikephil.charting.data.BarEntry
import com.google.android.libraries.places.api.net.PlacesClient
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.R
import com.business.zyvo.adapter.guest.ActivitiesAdapter
import com.business.zyvo.adapter.guest.AmenitiesAdapter
import com.business.zyvo.databinding.ActivityFiltersBinding
import com.business.zyvo.fragment.guest.FullScreenDialogFragment
import com.business.zyvo.locationManager.LocationManager
import com.business.zyvo.model.ActivityModel

class FiltersActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityFiltersBinding
    private lateinit var selectedItemTextView: TextView
    private lateinit var popupWindow: PopupWindow
    private val items = listOf("1 Hour", "2 Hour", "3 Hour", "4 Hour", "5 Hour")
    private lateinit var placesClient: PlacesClient
    private lateinit var autocompleteTextView: AutoCompleteTextView
    private lateinit var activityList : MutableList<ActivityModel>
    private lateinit var amenitiesList :MutableList<String>
    private lateinit var adapterActivity :ActivitiesAdapter
    private lateinit var adapterActivity2 :ActivitiesAdapter
    private lateinit var amenitiesAdapter :AmenitiesAdapter
    private lateinit var languageAdapter:AmenitiesAdapter
    private lateinit var dateManager :DateManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFiltersBinding.inflate(LayoutInflater.from(this))
        dateManager = DateManager(this)
        setContentView(binding.root)
        settingDataToActivityModel()
        adapterActivity = ActivitiesAdapter(this,activityList.subList(0,3))
        adapterActivity2 = ActivitiesAdapter(this,activityList.subList(3,activityList.size))
        amenitiesAdapter = AmenitiesAdapter(this, mutableListOf())
        languageAdapter = AmenitiesAdapter(this, mutableListOf())
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        selectedItemTextView = binding.tvHour
        settingDataToActivityModel()
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.llLocation, InputMethodManager.SHOW_IMPLICIT)

        clickListenerCalls()
        callingPriceRangeGraphSelection()
        setUpRecyclerView()

        var locationManager = LocationManager(this)
        locationManager.autoCompleteLocationWork(binding.autocompleteLocation)

        byDefaultSelectAvailability()
        settingBackgroundTaskToPeople()
        settingBackgroundTaskToProperty()
        settingBackgroundTaskToParking()
        settingBackgroundTaskToBedroom()
        settingBackgroundTaskToBathroom()
        binding.allowPets.setOnClickListener({
            showPopupWindowForPets(it)
        })
        showingMoreText()
        showingMoreAmText()
    }

    private fun showingMoreText() {
        val text = "Show More"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.underlinedTextView.text = spannableString
        binding.underlinedTextView.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.underlinedTextView.paint.isAntiAlias = true
        binding.underlinedTextView.setOnClickListener {
            languageAdapter.updateAdapter(getNationalLanguages())
           // binding.underlinedTextView.visibility =View.GONE
            showingLessText()
        }




    }


    private fun showingLessText() {
        val text = "Show Less"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.underlinedTextView.text = spannableString
        binding.underlinedTextView.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.underlinedTextView.paint.isAntiAlias = true
        binding.underlinedTextView.setOnClickListener {
            languageAdapter.updateAdapter(getNationalLanguages().subList(0,6))
            showingMoreText()
        }




    }


    private fun showingMoreAmText() {
        val text = "Show More"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.underlinedTextView1.text = spannableString
        binding.underlinedTextView1.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.underlinedTextView1.paint.isAntiAlias = true
        binding.underlinedTextView1.setOnClickListener {
            amenitiesAdapter.updateAdapter(amenitiesList)
            // binding.underlinedTextView.visibility =View.GONE
            showingLessAmText()
        }




    }


    private fun showingLessAmText() {
        val text = "Show Less"
        val spannableString = SpannableString(text).apply {
            setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.underlinedTextView1.text = spannableString
        binding.underlinedTextView1.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        binding.underlinedTextView1.paint.isAntiAlias = true
        binding.underlinedTextView1.setOnClickListener {
            amenitiesAdapter.updateAdapter(amenitiesList.subList(0,6))
            showingMoreAmText()
        }




    }




    private fun settingBackgroundTaskToBedroom(){
        binding.tvAnyBedrooms.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv1Bathrooms.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv2Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tv3Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv4Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv5Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tv7Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv8Bathroom.setOnClickListener {
            binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bathrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bathroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bathroom.setBackgroundResource(R.drawable.bg_inner_select_white)
        }

    }

    private fun settingBackgroundTaskToBathroom(){

        binding.tvAnyBedrooms.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv1Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv2Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv3Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv4Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv5Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }


        binding.tv7Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv8Bedroom.setOnClickListener {
            binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Bedroom.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Bedroom.setBackgroundResource(R.drawable.bg_inner_select_white)
        }
    }

    private fun settingBackgroundTaskToProperty() {

            binding.tvAny.setOnClickListener {
                binding.tvAny.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }

            binding.tv250.setOnClickListener {
                binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv250.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }

            binding.tv350.setOnClickListener {
                binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv350.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }


            binding.tv450.setOnClickListener {
                binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv450.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }
            binding.tv550.setOnClickListener {
                binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv550.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }

            binding.tv650.setOnClickListener {
                binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv650.setBackgroundResource(R.drawable.bg_inner_select_white)
                binding.tv750.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }
            binding.tv750.setOnClickListener {
                binding.tvAny.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv250.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv350.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv450.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv550.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv650.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tv750.setBackgroundResource(R.drawable.bg_inner_select_white)
            }
        }

    private fun byDefaultSelectAvailability()
    {
        binding.tvAny.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyBedrooms.setBackgroundResource(R.drawable.bg_inner_select_white)
        binding.tvAnyBathroom.setBackgroundResource(R.drawable.bg_inner_select_white)

    }

    private fun settingBackgroundTaskToPeople() {
        //No of people

        binding.tvAnyPeople.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }


        binding.tv1.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }
        binding.tv2.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv3.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv5.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv7.setOnClickListener {
            binding.tvAnyPeople.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7.setBackgroundResource(R.drawable.bg_inner_select_white)
        }
    }

    private fun settingBackgroundTaskToParking(){
        binding.tvAnyParkingSpace.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv1Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv2Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv3Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }


        binding.tv4Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv5Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv7Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
        }

        binding.tv8Parking.setOnClickListener {
            binding.tvAnyParkingSpace.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv1Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv2Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv3Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv4Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv5Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv7Parking.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tv8Parking.setBackgroundResource(R.drawable.bg_inner_select_white)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setUpRecyclerView(){

        val gridLayoutManager = GridLayoutManager(this, 3) // Set 4 columns
        val gridLayoutManager2 = GridLayoutManager(this,3)
        val gridLayoutManager3 = GridLayoutManager(this,2)
        binding.recyclerActivity2.layoutManager = gridLayoutManager2
        binding.recyclerActivity.layoutManager = gridLayoutManager
        binding.recyclerActivity2.isNestedScrollingEnabled = false
        binding.recyclerActivity.adapter = adapterActivity
        binding.recyclerLanguage.isNestedScrollingEnabled = false
        binding.recyclerActivity.isNestedScrollingEnabled = false

        binding.recyclerActivity2.adapter = adapterActivity2
        binding.recyclerActivity2.visibility =View.GONE
        binding.recyclerLanguage.layoutManager = gridLayoutManager3
        binding.recyclerLanguage.adapter= languageAdapter
        languageAdapter.updateAdapter(getNationalLanguages().subList(0,6))


        binding.tvOtherActivity.setOnClickListener {
            if(binding.recyclerActivity2.visibility == View.VISIBLE){
                binding.recyclerActivity2.visibility =View.GONE
            }
            else{
                binding.recyclerActivity2.visibility = View.VISIBLE
                binding.recyclerActivity2.scrollToPosition(activityList.size-3)
            }
        }

        //Amenities

        binding.recyclerAmenties.layoutManager = GridLayoutManager(this,2)
        binding.recyclerAmenties.adapter = amenitiesAdapter
        amenitiesAdapter.updateAdapter(amenitiesList.subList(0,6))

       var p= dateManager.getCurrentMonthAndYear()
        binding.tvMonthName.setText(p.first)
        binding.tvYear.setText(p.second.toString())

        binding.llDate.setOnClickListener {
            if(binding.rlDateSelection.visibility ==View.VISIBLE){
                binding.rlDateSelection.visibility = View.GONE
            }else{
                binding.rlDateSelection.visibility = View.VISIBLE
            }
        }

        binding.rlMonth.setOnClickListener {
            dateManager.showMonthSelectorDialog { selectedMonth ->
                binding.tvMonthName.text = selectedMonth
            }
        }

        binding.rlYearView.setOnClickListener {
            dateManager.showYearPickerDialog{
                year-> binding.tvYear.text = year.toString()

            }
        }

        binding.rlSave.setOnClickListener {
            binding.tvDateSelect.setText(binding.tvMonthName.text.toString()+" / "+binding.tvYear.text.toString())
            binding.rlDateSelection.visibility = View.GONE

        }

    }

    fun keyboardUp(){

    }

    private fun callingPriceRangeGraphSelection(){
        val barEntrys = ArrayList<BarEntry>()
        var seekBar = binding.seekBar

//        barEntrys.add(BarEntry(1.0f, 5.0f))
//        barEntrys.add(BarEntry(2.0f, 7.0f))
//        barEntrys.add(BarEntry(3.0f, 10.0f))

        val heights = arrayOf(5f, 3f, 4f, 7f, 8f, 15f, 13f, 12f, 10f, 5f, 17f, 16f, 13f, 12f, 8f,
            13f,10f,6f,9f,11f,7f)

        for (i in heights.indices) {
            barEntrys.add(BarEntry(i.toFloat(), heights[i]))
        }

        seekBar.setEntries(barEntrys)

        seekBar.onRangeChanged = { leftPinValue, rightPinValue ->
            val leftVal = (leftPinValue?.toInt()?.div(2))?.times(10)
            val rightVal = (rightPinValue?.toInt()?.div(2))?.times(10)
            binding.tvMinimumVal.setText("$"+leftVal.toString())
            binding.tvMaximumValue.setText("$"+rightVal.toString())
        }


    }

    private fun clickListenerCalls() {
        binding.tvHomeSetup.setOnClickListener(this)
        binding.tvRoom.setOnClickListener(this)
        binding.tvEntireHome.setOnClickListener(this)
        binding.llDate.setOnClickListener(this)
        binding.llTime.setOnClickListener {
            DateManager(this).showHourSelectionDialog(this) { selectedHour ->
                binding.tvHour.setText(selectedHour)
            }
        }
        byDefaultSelect()

        binding.imageFilter.setOnClickListener {
            val dialog = FullScreenDialogFragment()
            dialog.show(supportFragmentManager, "FullScreenDialog")
        }



    }

    private fun byDefaultSelect() {
        binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_manage_place)
    }

    private fun showDropdown(anchorView: View) {
        // Inflate the dropdown layout

//        Log.d("TESTING_ZYVOO","Here is Layout")
//        val popupView = LayoutInflater.from(this).inflate(R.layout.dropdown_item_time, null)
//        val dropdownLayout = LinearLayout(this)
//        dropdownLayout.orientation = LinearLayout.VERTICAL
//
//        // Create TextViews for each item
//        for (item in items) {
//            val textView = LayoutInflater.from(this).inflate(R.layout.dropdown_item_time, dropdownLayout, false) as TextView
//            textView.text = item
//            textView.setOnClickListener {
//                selectedItemTextView.text = item
//                popupWindow.dismiss()
//                Toast.makeText(this, "Selected: $item", Toast.LENGTH_SHORT).show()
//            }
//            dropdownLayout.addView(textView)
//        }
//
//        // Create the PopupWindow
//       // popupWindow = PopupWindow(dropdownLayout, dropdownLayout.measuredWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)
//
//
//        popupWindow = PopupWindow(dropdownLayout,
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            true)
//      //  var borderDrawable: Drawable = ContextCompat.getDrawable(this, R.drawable.bg_four_side_grey_corner)!!
//       // popupWindow.background = borderDrawable
//
//
//        popupWindow.isFocusable = true
//
//        // Show the PopupWindow
//        popupWindow.showAsDropDown(anchorView, 0, 0)


        val dropdownView = LayoutInflater.from(this).inflate(R.layout.dropdown_item_time, null)

        // Create the PopupWindow
        popupWindow = PopupWindow(dropdownView,
           250,
           400,
            true)

        // Set up click listeners for each item in the dropdown
        dropdownView.findViewById<TextView>(R.id.item_1).setOnClickListener {
            selectedItemTextView.text = "1 Hour"
            popupWindow.dismiss()

        }

        dropdownView.findViewById<TextView>(R.id.item_2).setOnClickListener {
            selectedItemTextView.text = "2 Hour"
            popupWindow.dismiss()

        }

        dropdownView.findViewById<TextView>(R.id.item_3).setOnClickListener {
            selectedItemTextView.text = "3 Hour"
            popupWindow.dismiss()

        }
        dropdownView.findViewById<TextView>(R.id.item_4).setOnClickListener{
            selectedItemTextView.text = "4 Hour"
            popupWindow.dismiss()

        }
        dropdownView.findViewById<TextView>(R.id.item_5).setOnClickListener {
            selectedItemTextView.text = "5 Hour"
            popupWindow.dismiss()

        }
        dropdownView.findViewById<TextView>(R.id.item_6).setOnClickListener {
            selectedItemTextView.text = "6 Hour"
            popupWindow.dismiss()

        }

        // Show the PopupWindow
//        popupWindow.showAsDropDown(anchorView, 0,anchorView.height)




        dropdownView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val dropdownHeight = dropdownView.measuredHeight

        // Get the location of the anchor view on the screen
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Calculate the Y position to show the PopupWindow below the anchor view
        val yPosition = location[1] + anchorView.height // Bottom of the anchor view

        // Show the PopupWindow at the calculated position
        popupWindow.showAtLocation(anchorView.rootView, Gravity.NO_GRAVITY, location[0], yPosition)






//        dropdownView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
//        val dropdownHeight = dropdownView.measuredHeight
//
//        // Show the PopupWindow at the bottom of the anchor view
//        popupWindow.showAtLocation()

     }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Prevent closing of suggestions when pressing the down arrow key
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            autocompleteTextView.showDropDown()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tv_home_setup -> {
               Log.d("TESTING_VOOPON","Here in the home setup")
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_inner_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }
            R.id.tv_room -> {
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_inner_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_outer_manage_place)
            }

            R.id.tv_entire_home -> {
                binding.tvHomeSetup.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvRoom.setBackgroundResource(R.drawable.bg_outer_manage_place)
                binding.tvEntireHome.setBackgroundResource(R.drawable.bg_inner_manage_place)
            }

        }

    }

    fun settingDataToActivityModel(){
        activityList = mutableListOf<ActivityModel>()
        amenitiesList = mutableListOf()

        amenitiesList.add("Wifi")
        amenitiesList.add("Kitchen")
        amenitiesList.add("Washer")
        amenitiesList.add("Dryer")
        amenitiesList.add("Air conditioning")
        amenitiesList.add("Heating")
        amenitiesList.add("Wifi")
        amenitiesList.add("Kitchen")
        amenitiesList.add("Washer")
        amenitiesList.add("Dryer")
        amenitiesList.add("Air conditioning")
        amenitiesList.add("Heating")



        var model1 = ActivityModel()
        model1.name = "Stays"
        model1.image = R.drawable.ic_stays
        activityList.add(model1)

        var model2 = ActivityModel()
        model2.name = "Event Space"
        model2.image = R.drawable.ic_event_space
        activityList.add(model2)

        var model3 = ActivityModel()
        model3.name = "Photo shoot"
        model3.image = R.drawable.ic_photo_shoot
        activityList.add(model3)

        var model4 = ActivityModel()
        model4.name = "Meeting"
        model4.image = R.drawable.ic_meeting
        activityList.add(model4)



        var model5 = ActivityModel()
        model5.name = "Party"
        model5.image = R.drawable.ic_party
        activityList.add(model5)


        var model6 = ActivityModel()
        model6.name = "Film Shoot"
        model6.image = R.drawable.ic_film_shoot
        activityList.add(model6)

        var model7 = ActivityModel()
        model7.name = "Performance"
        model7.image = R.drawable.ic_performance
        activityList.add(model7)

        var model8 = ActivityModel()
        model8.name = "Workshop"
        model8.image = R.drawable.ic_workshop
        activityList.add(model8)

        var model9 = ActivityModel()
        model9.name = "Corporate Event"
        model9.image = R.drawable.ic_corporate_event
        activityList.add(model9)

        var model10 = ActivityModel()
        model10.name = "Wedding"
        model10.image = R.drawable.ic_weding
        activityList.add(model10)

        var model11 = ActivityModel()
        model11.name = "Dinner"
        model11.image = R.drawable.ic_dinner
        activityList.add(model11)

        var model12 = ActivityModel()
        model12.name = "Retreat"
        model12.image = R.drawable.ic_retreat
        activityList.add(model12)


        var model13 = ActivityModel()
        model13.name = "Pop-up"
        model13.image = R.drawable.ic_popup_people
        activityList.add(model13)

        var model14 = ActivityModel()
        model14.name = "Networking"
        model14.image = R.drawable.ic_networking
        activityList.add(model14)

        var model15 = ActivityModel()
        model15.name = "Fitness Class"
        model15.image = R.drawable.ic_fitness_class
        activityList.add(model15)

        var model16 = ActivityModel()
        model16.name = "Audio Recording"
        model16.image = R.drawable.ic_audio_recording
        activityList.add(model16)

    }

        private fun getNationalLanguages(): MutableList<String> {
            return listOf(
                "Pashto, Dari",            // Afghanistan
                "Albanian",                // Albania
                "Arabic","Berber",          // Algeria
                "Catalan",                 // Andorra
                "Portuguese",              // Angola
                "Spanish",                 // Argentina
                "Armenian",                // Armenia
                "English",                 // Australia
                "German",                  // Austria
                "Azerbaijani",             // Azerbaijan
                "Bengali",                 // Bangladesh
                "Dutch", "French", "German",   // Belgium
                "Portuguese",              // Brazil
                 "French",         // Canada
                "Mandarin",                // China
                "Spanish",                 // Colombia
                "Danish",                  // Denmark
                "Arabic",                  // Egypt
                "Finnish", "Swedish",        // Finland
                                  // France
                "German",                  // Germany
                "Hindi","English",          // India
                "Italian",                 // Italy
                "Japanese",                // Japan
                "Spanish",                 // Mexico
                "Dutch",                   // Netherlands
                 "Māori",          // New Zealand
                                 // Nigeria
                "Urdu",           // Pakistan
                "Russian",                 // Russia
                  // South Africa
                "Spanish",                 // Spain
                "Swedish",                 // Sweden
                "German", // Switzerland
                               // United States
                "Vietnamese"               // Vietnam
                // Add more languages as needed
            ).toMutableList()

    }



    private fun showPopupWindowForPets(anchorView: View) {
        // Inflate the popup layout
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_layout_pets, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT
            ,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        // Show the popup window at the bottom right of the TextView

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(anchorView, anchorView.width, 0)
    }

}