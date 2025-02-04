package com.business.zyvo.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.business.zyvo.R
import com.business.zyvo.model.ActivityModel
import com.business.zyvo.model.host.ItemRadio
import java.io.ByteArrayOutputStream
import java.io.InputStream

import android.util.Base64
import java.io.IOException

object PrepareData {

    fun getOnlyAmenitiesList() : MutableList<Pair<String,Boolean>>{
        val amenitiesList = mutableListOf(
            "Free Parking",
            "Meal Included",
            "Elevator/Lift Access",
            "Wheelchair Accessible",
            "Smoking Allowed",
            "Non-Smoking Property",
            "Security Cameras",
            "Concierge Service",
            "Airport Shuttle Service",
            "Bike Rental",
            "Business Centre",
            "Conference/Meeting Facilities",
            "Spa/Wellness Centre",
            "Outdoor Space (Garden, Terrace)",
            "BBQ/Grill Area",
            "Games Room",
            "Ski-In/Ski-Out Access",
            "Waterfront Property",
            "Scenic Views",
            "Eco-Friendly/Green Certified",
            "Smart Home Technology",
            "Electric Vehicle Charging Station",
            "Yoga/Meditation Space",
            "On-Site Restaurant/Cafe",
            "Bar/Lounge Area",
            "Live Entertainment",
            "Pet Amenities (Pet Sitting, Pet Spa)",
            "Sports Facilities (Tennis Court, Golf Course)",
            "Cultural Experiences/Workshops"
        )

     return   amenitiesList.flatMap { language ->
            language.split(", ").map { it.trim() to false }
        }.toMutableList()

    }

    fun getAmenitiesList() :Pair<MutableList<ActivityModel>, MutableList<String>>{
        val activityList = mutableListOf<ActivityModel>()
        val amenitiesList = mutableListOf(
                "Free Parking", "Meal Included", "Elevator/Lift Access", "Wheelchair Accessible", "Smoking Allowed",
                "Non-Smoking Property", "Security Cameras", "Concierge Service", "Airport Shuttle Service",
                "Bike Rental", "Business Centre",
                "Conference/Meeting Facilities",
                "Spa/Wellness Centre",
                "Outdoor Space",
                "BBQ/Grill Area",
                "Games Room",
                "Ski-In/Ski-Out Access",
                "Waterfront Property",
                "Scenic Views",
                "Eco-Friendly/Green Certified",
                "Smart Home Technology",
                "Electric Vehicle Charging Station",
                "Yoga/Meditation Space",
                "On-Site Restaurant/Cafe",
                "Bar/Lounge Area",
                "Live Entertainment",
                "Pet Amenities",
                "Sports Facilities",
                "Cultural Experiences/Workshops"
            )



        val model1 = ActivityModel()
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

        return Pair(activityList,amenitiesList)
    }

    fun getEventEquipment(): List<String> {
        return listOf(
            "Computer Screen",
            "Studio Lights",
            "Projectors",
            "Speakers",
            "Microphones",
            "Sounds Systems",
            "DJ Equipment",
            "Tables",
            "Chairs",
            "Stage Platforms",
            "Art Supplies (Paint, brushes)",
            "Allow Alcohol",
            "Onsite Food Prep (Event)",
            "Extra Person above Max Capacity",
            "Photographer (Per Hour)",
            "Videographer (Per Hour)"
        )
    }


    fun getHourMinimumList(): MutableList<ItemRadio> {
        return mutableListOf(
            ItemRadio("1 hour minimum", false),
            ItemRadio("2 hour minimum", false),
            ItemRadio("3 hour minimum", false),
            ItemRadio("4 hour minimum", false),
            ItemRadio("5 hour minimum", false),
            ItemRadio("6 hour minimum", false),
            ItemRadio("7 hour minimum", false),
            ItemRadio("8 hour minimum", false),
            ItemRadio("9 hour minimum", false),
            ItemRadio("10 hour minimum", false),
            ItemRadio("11 hour minimum", false),
            ItemRadio("12 hour minimum", false),
            ItemRadio("13 hour minimum", false),
            ItemRadio("14 hour minimum", false),
            ItemRadio("15 hour minimum", false),
            ItemRadio("16 hour minimum", false),
            ItemRadio("17 hour minimum", false),
            ItemRadio("18 hour minimum", false),
            ItemRadio("19 hour minimum", false),
            ItemRadio("20 hour minimum", false),
            ItemRadio("21 hour minimum", false),
            ItemRadio("22 hour minimum", false),
            ItemRadio("23 hour minimum", false),
            ItemRadio("24 hour minimum", false)
        )
    }

    fun getDiscountList(): MutableList<ItemRadio> {
        return mutableListOf(
            ItemRadio("5% Discount", false),
            ItemRadio("10% Discount", false),
            ItemRadio("15% Discount", false),
            ItemRadio("20% Discount", false),
            ItemRadio("25% Discount", false),
            ItemRadio("30% Discount", false),
            ItemRadio("35% Discount", false),
            ItemRadio("40% Discount", false),
            ItemRadio("45% Discount", false),
            ItemRadio("50% Discount", false),
            ItemRadio("75% Discount", false)
        )
    }

    fun getPriceAndHourList(): MutableList<ItemRadio> {
        return mutableListOf(
            ItemRadio("$10 per hour", false),
            ItemRadio("$20 per hour", false),
            ItemRadio("$30 per hour", false),
            ItemRadio("$40 per hour", false),
            ItemRadio("$50 per hour", false),
            ItemRadio("$60 per hour", false),
            ItemRadio("$70 per hour", false),
            ItemRadio("$80 per hour", false),
            ItemRadio("$90 per hour", false),
            ItemRadio("$100 per hour", false),
            ItemRadio("$110 per hour", false),
            ItemRadio("$120 per hour", false),
            ItemRadio("$130 per hour", false),
            ItemRadio("14 hour minimum", false),
            ItemRadio("15 hour minimum", false),
            ItemRadio("16 hour minimum", false),
            ItemRadio("17 hour minimum", false),
            ItemRadio("18 hour minimum", false),
            ItemRadio("19 hour minimum", false),
            ItemRadio("20 hour minimum", false),
            ItemRadio("21 hour minimum", false),
            ItemRadio("22 hour minimum", false),
            ItemRadio("23 hour minimum", false),
            ItemRadio("24 hour minimum", false)
        )
    }

    fun getPriceAndHourList1(): MutableList<ItemRadio> {
        return mutableListOf(
            ItemRadio("$10 per hour", false),
            ItemRadio("$20 per hour", false),
            ItemRadio("$30 per hour", false),
            ItemRadio("$40 per hour", false),
            ItemRadio("$50 per hour", false),
            ItemRadio("$60 per hour", false),
            ItemRadio("$70 per hour", false),
            ItemRadio("$80 per hour", false),
            ItemRadio("$90 per hour", false),
            ItemRadio("$100 per hour", false),
            ItemRadio("$110 per hour", false),
            ItemRadio("$120 per hour", false),
            ItemRadio("$130 per hour", false),
            ItemRadio("14 hour minimum", false),
            ItemRadio("15 hour minimum", false),
            ItemRadio("16 hour minimum", false),
            ItemRadio("17 hour minimum", false),
            ItemRadio("18 hour minimum", false),
            ItemRadio("19 hour minimum", false),
            ItemRadio("20 hour minimum", false),
            ItemRadio("21 hour minimum", false),
            ItemRadio("22 hour minimum", false),
            ItemRadio("23 hour minimum", false),
            ItemRadio("24 hour minimum", false)
        )
    }

    fun getLanguagePairs(): MutableList<Pair<String, Boolean>> {
        val languageList = listOf(
            "Pashto, Dari",            // Afghanistan
            "Albanian",                // Albania
            "Arabic", "Berber",        // Algeria
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
            "French",                  // Canada
            "Mandarin",                // China
            "Spanish",                 // Colombia
            "Danish",                  // Denmark
            "Arabic",                  // Egypt
            "Finnish", "Swedish",      // Finland
            "French",                  // France
            "German",                  // Germany
            "Hindi", "English",        // India
            "Italian",                 // Italy
            "Japanese",                // Japan
            "Spanish",                 // Mexico
            "Dutch",                   // Netherlands
            "Māori",                   // New Zealand
            "Urdu",                    // Pakistan
            "Russian",                 // Russia
            "Spanish",                 // South Africa
            "Spanish",                 // Spain
            "Swedish",                 // Sweden
            "German",                  // Switzerland
            "English",                 // United States
            "Vietnamese"               // Vietnam
        )

        return languageList.flatMap { language ->
            language.split(", ").map { it.trim() to false }
        }.toMutableList()
    }



    fun uriToBase64(uri: Uri, contentResolver: ContentResolver, maxWidth: Int = 1024, maxHeight: Int = 1024): String? {
        try {
            // Open the InputStream from the URI
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let {
                // Decode the image to a Bitmap
                val originalBitmap = BitmapFactory.decodeStream(it)

                // Resize the Bitmap to fit within the specified max width and height
                val resizedBitmap = resizeBitmap(originalBitmap, maxWidth, maxHeight)

                // Compress the resized Bitmap into a ByteArrayOutputStream
                val byteArrayOutputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream) // 85 is the quality (out of 100)

                // Convert the byte array to a Base64 string
                val byteArray = byteArrayOutputStream.toByteArray()
                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    // Function to resize the Bitmap maintaining aspect ratio
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Calculate the scaling factor to maintain the aspect ratio
        val scaleFactor = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

        // Calculate the new width and height
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        // Create a resized Bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

}