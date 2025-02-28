package com.business.zyvo.fragment.both.browseArticleHost.model

data class BrowseArticleModel(
    val success: Boolean,
    val message: String?,
    val code: Int,
    val data: MutableList<Article>?
)

data class Article(
    val id: Int,
    val title: String?,
    val description: String?="",
    val cover_image: String?
)