package com.business.zyvo.model

data class WishListDetailModel(
    val hourly_rate: String?,
    val images: List<String>?,
    val is_in_wishlist: Int,
    val is_instant_book: Int,
    val property_id: Int,
    val rating: Double,
    val review_count: Int,
    val title: String?,
    val wishlist_item_id: Int
)