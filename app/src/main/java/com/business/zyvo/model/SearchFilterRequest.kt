package com.business.zyvo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchFilterRequest(
    var user_id: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val date: String = "",
    val hour: String = "",
    val start_time: String = "",
    val end_time: String = "",
    val location:String = "",
    val activity: String = ""):Parcelable

