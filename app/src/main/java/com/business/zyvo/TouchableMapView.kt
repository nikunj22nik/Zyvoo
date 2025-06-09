package com.business.zyvo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView

class TouchableMapView : MapView {
    var touchDown: (() -> Unit)? = null
    var touchUp: (() -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context,
        attrs!!, defStyle)
    constructor(context: Context, options: GoogleMapOptions?) : super(context, options)

    private val touchableWrapper = TouchableWrapper(context).apply {
        addView(this@TouchableMapView)
    }

    inner class TouchableWrapper @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr) {
        init {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> touchDown?.invoke()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> touchUp?.invoke()
            }
            return super.dispatchTouchEvent(ev)
        }
    }
}