package com.business.zyvo.fragment.both.notificationfragment

data class NotificationRootModel(
    val created_at: String,
    val `data`: Any,
    val is_read: Boolean,
    val message: String,
    val notification_id: Int,
    val title: String,
    val type: String,
    val user_id: Int,
    val user_type: String
)