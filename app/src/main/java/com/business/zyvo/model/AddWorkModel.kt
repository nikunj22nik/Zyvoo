package com.business.zyvo.model

import com.business.zyvo.fragment.both.completeProfile.HasName

data class AddWorkModel(
    override var name: String

): HasName
