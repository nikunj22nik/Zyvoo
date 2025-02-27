package com.business.zyvo.model

data class filterDataModel(
    val code: Int,
    val `data`: List<Datas>,
    val message: String,
    val success: Boolean
)

data class Datas(
    val distance: String,
    val hourly_rate: String,
    val images: List<String>,
    val is_in_wishlist: Any,
    val is_instant_book: Int,
    val property_id: Int,
    val property_name: String,
    val rating: String,
    val review_count: String
)