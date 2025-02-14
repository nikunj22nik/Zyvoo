package com.business.zyvo.fragment.guest.helpCenter.model


data class HelpCenterResponse(
    val success: Boolean,
    val message: String,
    val code: Int,
    val data: HelpCenterData
)

data class HelpCenterData(
    val user_id: Int,
    val user_fname: String,
    val user_lname: String,
    val guides: MutableList<Guide>,
    val articles: MutableList<Article>
)

data class Guide(
    val id: Int,
    val title: String,
    val cover_image: String
)

data class Article(
    val id: Int,
    val title: String,
    val description: String
)

