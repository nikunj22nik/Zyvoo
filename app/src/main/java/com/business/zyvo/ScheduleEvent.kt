package com.business.zyvo

data class ScheduleEvent(
    val title: String,
    val subtitle: String,
    val time: String,
    val column: Int,
    val row: Int,
    val drawableRes: Int
)
