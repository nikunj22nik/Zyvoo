package com.business.zyvo.fragment.guest.home.model

data class FilterDataMode<T>(
    val success: Boolean = false,
    val message: String? = null,
    val code: Int = 0,
    val data: T? = null
)
