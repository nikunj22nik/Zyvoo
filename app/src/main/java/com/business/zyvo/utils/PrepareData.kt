package com.business.zyvo.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Base64
import com.business.zyvo.R
import com.business.zyvo.model.ActivityModel
import com.business.zyvo.model.host.ItemRadio
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object PrepareData {


    fun monthNameToNumber(monthName: String): Int {
        val months = listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )

        return months.indexOf(monthName) + 1  // Adding 1 because the list index is 0-based
    }

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

    @Throws(ParseException::class)
    fun getMyPrettyDate(neededTimeMilis: String?): String {
        val nowTime = Calendar.getInstance()
        nowTime.timeZone = TimeZone.getTimeZone("UTC")


        val neededTime = Calendar.getInstance()
        nowTime.timeZone = TimeZone.getTimeZone("UTC")

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(neededTimeMilis)
        val millis = date.time
        neededTime.timeInMillis = millis


        return if ((neededTime[Calendar.YEAR] == nowTime[Calendar.YEAR])) {
            if ((neededTime[Calendar.MONTH] == nowTime[Calendar.MONTH])) {
                if (neededTime[Calendar.DATE] - nowTime[Calendar.DATE] == 1) {
                    //here return like "Tomorrow at 12:00"
                    " Tomorrow "
                } else if (nowTime[Calendar.DATE] == neededTime[Calendar.DATE]) {
                    //here return like "Today at 12:00"
                    " " + DateFormat.format("hh:mm aa", neededTime).toString().uppercase(
                        Locale.getDefault()
                    )
                } else if (nowTime[Calendar.DATE] - neededTime[Calendar.DATE] == 1) {
                    //here return like "Yesterday at 12:00"
                    //                    return " Yesterday at " + DateFormat.format("hh:mm aa", neededTime).toString().toUpperCase();
                    " Yesterday "
                } else {
                    //here return like "May 31, 12:00"
                    DateFormat.format(" MMMM, dd", neededTime).toString().uppercase(
                        Locale.getDefault()
                    )
                }
            } else {
                //here return like "May 31, 12:00"
                DateFormat.format(" MMMM, dd", neededTime).toString().uppercase(
                    Locale.getDefault()
                )
            }
        } else {
            //here return like "May 31 2010, 12:00" - it's a different year we need to show it
            //            return DateFormat.format("MMMM dd yyyy, hh:mm aa", neededTime).toString().toUpperCase();
            DateFormat.format("MMMM, dd yyyy", neededTime).toString().uppercase(
                Locale.getDefault()
            )
        }
    }


    @JvmStatic
    fun updateLastMsgTime(time: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val secondDate = dateFormat.parse(time)
            val currentCalendar = Calendar.getInstance()
            val firstDate = currentCalendar.time

            val dateComponents = calculateDateComponents(secondDate, firstDate)

            when {
                dateComponents.year > 0 -> "${dateComponents.year}y ago"
                dateComponents.month > 0 -> "${dateComponents.month} month ago"
                dateComponents.day > 0 -> "${dateComponents.day}d ago"
                dateComponents.hour > 0 -> "${dateComponents.hour}h ago"
                dateComponents.minute > 0 -> {
                    if (dateComponents.minute >= 1) {
                        "${dateComponents.minute}m ago"
                    } else {
                        "now"
                    }
                }
                else -> "now"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            "now"
        }
    }

    private fun calculateDateComponents(startDate: Date, endDate: Date): DateComponents {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        val start = calendar.timeInMillis

        calendar.time = endDate
        val end = calendar.timeInMillis

        var diff = end - start

        val years = diff / (365 * 24 * 60 * 60 * 1000)
        diff %= (365 * 24 * 60 * 60 * 1000)

        val months = diff / (30 * 24 * 60 * 60 * 1000)
        diff %= (30 * 24 * 60 * 60 * 1000)

        val days = diff / (24 * 60 * 60 * 1000)
        diff %= (24 * 60 * 60 * 1000)

        val hours = diff / (60 * 60 * 1000)
        diff %= (60 * 60 * 1000)

        val minutes = diff / (60 * 1000)

        return DateComponents(years.toInt(), months.toInt(), days.toInt(), hours.toInt(), minutes.toInt())
    }

    data class DateComponents(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int
    )

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

    fun numberOFColumn(activity :Activity) :Int{

        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // Calculate the number of columns based on screen width
        val columns: Int = when {
            screenWidth > 1800 ->  return 4  // Large screens (e.g., tablets or large devices)
            screenWidth > 1000 -> return 3  // Medium screens (e.g., some tablets)
            else -> return 2  // Small screens (e.g., phones)
        }

    }

    fun getPath(context: Context, uri: Uri): String? {
        var uri = uri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor =
                    context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor!!.moveToFirst()) {
                    return cursor!!.getString(column_index)
                }
            } catch (e: Exception) {
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

}