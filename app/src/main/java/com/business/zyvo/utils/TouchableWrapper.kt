package com.business.zyvo.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class TouchableWrapper@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var onTouchListener: (() -> Unit)? = null

    fun setOnTouchCallback(callback: () -> Unit) {
        onTouchListener = callback
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_MOVE) {
            onTouchListener?.invoke()
        }
        return super.dispatchTouchEvent(ev)
    }
}

