package com.business.zyvo.utils

import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout

class TouchableWrapper(context: Context) : FrameLayout(context) {
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}
