package com.business.zyvo.fragment.host.payments.model

data class GetBookingResponse(
    val success: Boolean,
    val message: String,
    val code: Int,
    val data: MutableList<GetBookingList>?
)

data class GetBookingList(
    val booking_id: Int,
    val booking_amount: String?,
    val status: String?,
    val booking_date: String?,
    val guest_name: String?,
    val guest_profile_image: String?
)
