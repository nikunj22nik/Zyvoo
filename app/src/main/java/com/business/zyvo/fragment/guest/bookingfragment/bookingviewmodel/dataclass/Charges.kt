package com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass

import com.google.gson.annotations.SerializedName

data class Charges(
    @SerializedName("add-on")
    val add_on: Int? = 0,
    val discount: Int? = 0,
    val booking_amount: String? = "0.0",
    val booking_hours: Int? = 0,
    val bulk_discount_hours: Any? = null,
    val bulk_discount_rate: String? = "0.0",
    val cleaning_fee: String? = "0.0",
    val hourly_rate: String? = "0.0",
    val min_booking_hours: String? = "0.0",
    val taxes: String? = "0.0",
    val total: String? = "0.0",
    val zyvo_service_fee: String? = "0.0"
)
