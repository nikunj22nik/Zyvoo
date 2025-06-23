package com.business.zyvo.fragment.guest.home.model

data class HomePropertyData( val images: List<String>,
                             val property_id: Int,
                             var title: String,
                             val hourly_rate: String,
                             val is_instant_book: Int,
                             var is_in_wishlist: Int,
                             val is_star_host: String,
                             val rating: String,
                             val review_count: String,
                             val latitude: String,
                             val longitude: String,
                             val distance_miles: String,
                             val host_name: String,
                             val host_profile_image: String,
                             val host_address: String)

