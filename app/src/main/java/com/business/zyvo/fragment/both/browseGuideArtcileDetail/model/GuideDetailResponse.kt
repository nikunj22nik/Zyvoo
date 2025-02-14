package com.business.zyvo.fragment.both.browseGuideArtcileDetail.model

data class GuideDetailResponse(
    val success: Boolean,
    val message: String,
    val code: Int,
    val data: GuideDetails
)


data class GuideDetails(
    val guide_id: Int,
    val title: String?,
    val time_required: String?,
    val date: String?,
    val description: String?,
    val cover_image: String?,
    val author_name: String?,
    val category: String?
)

