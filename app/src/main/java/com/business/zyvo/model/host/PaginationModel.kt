package com.business.zyvo.model.host

data class PaginationModel(
    val count: Int,
    val current_page: Int,
    val per_page: Int,
    val total: Int,
    val total_pages: Int
)