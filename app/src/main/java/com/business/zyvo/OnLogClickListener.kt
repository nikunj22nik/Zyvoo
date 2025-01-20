package com.business.zyvo

import com.business.zyvo.model.ViewpagerModel

interface OnLogClickListener {
    fun itemClick(items: MutableList<ViewpagerModel>)
}