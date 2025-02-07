package com.business.zyvo.model

import com.business.zyvo.fragment.both.completeProfile.HasName

data class AddLocationModel(
    override var name: String
): HasName
