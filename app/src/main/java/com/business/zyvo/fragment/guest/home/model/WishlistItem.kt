package com.business.zyvo.fragment.guest.home.model

data class WishlistItem( val wishlist_id: Int,
                         val wishlist_name: String,
                         val items_in_wishlist: Int,
                         val last_saved_property_id: Int,
                         val last_saved_property_image: String)
