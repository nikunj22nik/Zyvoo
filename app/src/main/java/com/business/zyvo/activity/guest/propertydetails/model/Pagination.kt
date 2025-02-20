package com.business.zyvo.activity.guest.propertydetails.model

data class Pagination( var total: Int = 0,
                       var count: Int = 0,
                       var per_page: Int = 0,
                       var current_page: Int = 0,
                       var total_pages: Int = 0)
