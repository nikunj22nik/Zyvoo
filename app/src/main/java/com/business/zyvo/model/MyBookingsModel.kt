package com.business.zyvo.model

data class MyBookingsModel(  val booking_date: String,
                             val booking_id: Int,
                             val booking_status: String,
                             val guest_avatar: String,
                             val guest_name: String,
                             val user_id: Int
    )
