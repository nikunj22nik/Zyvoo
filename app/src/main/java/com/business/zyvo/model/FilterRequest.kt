package com.business.zyvo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterRequest(
    val user_id: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val place_type: String = "",
    val minimum_price: String = "",
    val maximum_price: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val property_size: String = "",
    val bedroom: String = "",
    val bathroom: String = "",
    val parkingcount: String = "",
    val people_count: String = "",
    val instant_booking: String = "",
    val self_check_in: String = "",
    val allows_pets: String = "",
    val activities: List<String>?,
    val amenities: List<String>?,
    val languages: List<String>?
):Parcelable

