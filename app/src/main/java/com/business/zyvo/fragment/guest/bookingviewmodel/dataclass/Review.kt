package com.business.zyvo.fragment.guest.bookingviewmodel.dataclass

data class Review(
    val comment: String? = "No comment",
    val date: String? = "N/A",
    val image: String? = "",
    val name: String? = "Anonymous",
    val rating: Double? = 0.0
)
