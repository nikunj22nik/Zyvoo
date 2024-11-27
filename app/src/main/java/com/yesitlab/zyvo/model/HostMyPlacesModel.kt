package com.yesitlab.zyvo.model

data class HostMyPlacesModel(
    var textHotelName: String,
    var textRating: String,
    var textTotal: String,
    var textMiles: String,
    var textPricePerHours: String,
    var newList: MutableList<ViewpagerModel> = mutableListOf()
)