package com.yesitlab.zyvo

import com.yesitlab.zyvo.model.LogModel
import com.yesitlab.zyvo.model.ViewpagerModel

interface OnLogClickListener {
    fun itemClick(items: MutableList<ViewpagerModel>)
}