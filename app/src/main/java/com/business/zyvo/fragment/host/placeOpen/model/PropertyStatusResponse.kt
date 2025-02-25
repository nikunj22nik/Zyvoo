package com.business.zyvo.fragment.host.placeOpen.model

data class PropertyStatusResponse(
    val success: Boolean,
    val message: String,
    val code: Int,
    val data: PropertyStatusData
)

data class PropertyStatusData(
    val property_id: Int,
    val property_status: String?
)
