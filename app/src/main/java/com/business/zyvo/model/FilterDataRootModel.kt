package com.business.zyvo.model

data class FilterDataRootModel(
    val distance: String = "",
    val hourly_rate: String = "",
    val images: List<String> = emptyList(),
    val is_in_wishlist: Any = "",
    val is_instant_book: Int = -1,
    val property_id: Int = -1,
    val property_name: String = "",
    val rating: String = "",
    val review_count: String = ""
)