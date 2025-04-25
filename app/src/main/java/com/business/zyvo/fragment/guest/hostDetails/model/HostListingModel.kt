package com.business.zyvo.fragment.guest.hostDetails.model

import com.business.zyvo.fragment.guest.profile.model.PayoutData
import com.google.gson.annotations.SerializedName



data class HostListingModel(
    val success: Boolean,
     val message: String,
    val code: Int,
   val data: HostData?
)

data class HostData(
    val host: Host?,
    val about_host: AboutHost?,
    val properties: MutableList<Property>?

)

data class Host(
    val profile_picture: String? ="",
    val name: String? =""
)

data class AboutHost(
    val host_profession: List<String>?,
    val location: String?="",
    val language: List<String>?,
    val description: String? =""
)

data class Property(
    val property_id: Int? =-1,
    val host_id: Int? =-1,
    val host_name: String? ="",
    val title: String? ="",
    val hourly_rate: String? ="",
    val is_instant_book: Int? =-1,
    val reviews_total_rating: String? ="",
    val reviews_total_count: String? ="",
    val latitude: String? ="",
    val longitude: String? ="",
    val distance_miles: String? ="",
    val profile_image: String? ="",
    val property_images: MutableList<String>?
)