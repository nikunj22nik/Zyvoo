package com.business.zyvo

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class TouchableMapFragment : Fragment() {
    var touchDown: (() -> Unit)? = null
    var touchUp: (() -> Unit)? = null
    private var mapFragment: SupportMapFragment? = null
    private var mapReadyCallback: OnMapReadyCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val wrapper = TouchableWrapper(requireContext())
        wrapper.setBackgroundColor(resources.getColor(android.R.color.transparent))

        // Create and add SupportMapFragment
        mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(wrapper.id, mapFragment!!)
            .commit()

        wrapper.touchDown = {
            touchDown?.invoke()
        }
        wrapper.touchUp = {
            touchUp?.invoke()
        }

        return wrapper
    }

    fun getMapAsync(callback: OnMapReadyCallback) {
        mapReadyCallback = callback
        mapFragment?.getMapAsync(callback)
    }

    inner class TouchableWrapper @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr) {
        var touchDown: (() -> Unit)? = null
        var touchUp: (() -> Unit)? = null

        init {
            id = View.generateViewId() // Generate unique ID for this view
        }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> touchDown?.invoke()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> touchUp?.invoke()
            }
            return super.dispatchTouchEvent(ev)
        }
    }

    override fun onResume() {
        super.onResume()
        mapFragment?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapFragment?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapFragment?.onSaveInstanceState(outState)
    }
}