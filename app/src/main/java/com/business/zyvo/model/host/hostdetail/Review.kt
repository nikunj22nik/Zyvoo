package com.business.zyvo.model.host.hostdetail

data class Review(
    val profile_image: String,
    val review_date: String,
    val review_message: String,
    val review_rating: String,
    val reviewer_name: String
)