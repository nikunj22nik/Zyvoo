package com.business.zyvo

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class NoHorizontalScrollLayoutManager(context: Context) : LinearLayoutManager(context) {

    override fun canScrollHorizontally(): Boolean {
        return false
    }

}