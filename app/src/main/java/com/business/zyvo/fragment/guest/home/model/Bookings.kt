package com.business.zyvo.fragment.guest.home.model

import com.business.zyvo.activity.guest.propertydetails.model.AddOn

data class Bookings( val booking_id: Int,
                     val guest_user_id: Int,
                     val host_user_id: Int,
                     val property_id: Int,
                     val payment_id: String,
                     val booking_start: String,
                     val booking_end: String,
                     val booking_date: String,
                     val booking_hours: Int,
                     val booking_hourly_rate: String,
                     val cleaning_fee: String,
                     val service_fee: String,
                     val tax: String,
                     val final_booking_end: String,
                     val booking_amount: String,
                     val total_amount: String,
                     val discount_percent: String,
                     val extension_hours: Int?,
                     val booking_extension_amount: String?,
                     val status: String,
                     var selected_add_ons: List<AddOn>? = null)
