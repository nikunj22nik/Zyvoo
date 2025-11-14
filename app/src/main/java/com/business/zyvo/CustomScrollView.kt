package com.business.zyvo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class CustomScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs){

    private var shouldIntercept = true

    fun setShouldIntercept(intercept: Boolean) {
        shouldIntercept = intercept
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // If shouldIntercept is false, the ScrollView will not intercept touch events
        return if (shouldIntercept) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

}