package com.business.zyvo.model

data class ChannelListModel(
    val group_name: String?,
    val receiver_id: String?,
    val receiver_image: String?,
    val receiver_name: String?,
    val sender_id: String?,
    val sender_name: String?,
    val sender_profile: String?,
    var lastMessage :String ="",
    var lastMessageTime :String ="",
    var isOnline :Boolean = false,
    var property_title :String ="",
    var date :String ="",
    var is_blocked :Int =0,
    var is_favorite :Int = 0,
    var is_muted :Int = 0,
    var is_archived :Int = 0
)