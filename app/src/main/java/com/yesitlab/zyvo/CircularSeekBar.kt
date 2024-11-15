package com.yesitlab.zyvo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class CircularSeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF0000") // Background color (e.g., red)
        style = Paint.Style.FILL
    }


    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        elevation = 70f
        strokeWidth = 90f
    }

    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4AEAB1") // Greenish color
        style = Paint.Style.STROKE
        strokeWidth = 90f
        strokeCap = Paint.Cap.ROUND
    }

    private val paintThumb = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        strokeWidth = 90f
        //nikunj change
    }

    private val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66000000")
        strokeWidth = 5f
        style = Paint.Style.FILL
    }

    private val rectF = RectF()
    private var progressAngle = 5f // Start at 90 degrees (top)
    private var radius = 0f
    private var thumbX = 0f
    private var thumbY = 0f
    private var progress = 1 // Default to 1 hour
    private var maxHours = 13
    private var isTouchingThumb = false
    private val hourDots = mutableListOf<Pair<Float,Float>>() // Store hour dots' positions
    private var initialAngle = 10f

//    init {
//        setOnTouchListener { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    if (isTouchOnThumb(event.x, event.y)) {
//                        isTouchingThumb = true
//                        true
//                    } else {
//                        isTouchingThumb = false
//                        snapToNearestDot(event.x, event.y) // Snap if touch is near a dot
//                        true
//                    }
//
//                    true
//                }
//
//                MotionEvent.ACTION_MOVE -> {
////                    if (isTouchingThumb) {
////                        updateProgress(event.x, event.y)
////                        invalidate()
////                        true
////                    } else {
////                        false
////                    }
//
//                    if (isTouchingThumb) {
//                        val currentAngle = getAngleFromTouch(event.x, event.y)
//                        val angleDiff = currentAngle - initialAngle
//                        progressAngle = (progressAngle + angleDiff + 360) % 360
//
//                        // Update progress (hours) based on the new progressAngle
//                        progress = ((progressAngle / 360) * maxHours).toInt()
//                        progress = progress.coerceIn(0, maxHours - 1)
//
//                        updateCenterText()
//                        invalidate()
//
//                        // Reset initial angle to current for continuous movement
//                        initialAngle = currentAngle
//                        true
//                    } else {
//                        false
//                    }
//                }
//
//                MotionEvent.ACTION_UP -> {
//                    isTouchingThumb = false
//                    true
//                }
//
//                else -> false
//            }
//        }
//    }

    init {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isTouchOnThumb(event.x, event.y)) {
                        isTouchingThumb = true
                        initialAngle = getAngleFromTouch(event.x, event.y)
                        true
                    } else {
                        isTouchingThumb = false
                        snapToNearestDot(event.x, event.y)
                        true
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isTouchingThumb) {
                        val currentAngle = getAngleFromTouch(event.x, event.y)
                        val angleDiff = currentAngle - initialAngle
                        progressAngle = (progressAngle + angleDiff + 360) % 360

                        // Update progress (hours) based on the new progressAngle
                        progress = ((progressAngle / 360) * maxHours).toInt()
                        progress = progress.coerceIn(0, maxHours - 1)

                        updateCenterText()
                        invalidate()

                        // Reset initial angle to current for continuous movement
                        initialAngle = currentAngle
                        true
                    } else {
                        false
                    }
                }

                MotionEvent.ACTION_UP -> {
                    isTouchingThumb = false
                    true
                }

                else -> false
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        radius = min(width, height) / 2 - paintCircle.strokeWidth / 2
        val cx = width / 2
        val cy = height / 2

        // Draw the background circle
        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)

        canvas.drawArc(rectF, 0f, 360f, false, paintCircle)

        // Draw the progress arc
        canvas.drawArc(rectF, -90f, progressAngle, false, paintProgress)

        // Draw hour dots at each hour position
        hourDots.clear() // Clear the previous dot positions

        for (hour in 0 until maxHours) {
            val angle = Math.toRadians((hour * 360f / maxHours - 90).toDouble())
            val dotX = (cx + radius * cos(angle)).toFloat()
            val dotY = (cy + radius * sin(angle)).toFloat()
            hourDots.add(Pair(dotX, dotY))
            val dotProgressAngle = (hour * 360f / maxHours)
            // Check if the dot's angle is less than the current progress angle
            if (dotProgressAngle > progressAngle) {
                // Draw small dot only if not covered by progress arc
                canvas.drawCircle(dotX, dotY, 10f, paintDot)
            }
        }

        // Calculate the thumb position based on progress
        if (progress in hourDots.indices) {
            thumbX = hourDots[progress].first
            thumbY = hourDots[progress].second
        }

        // Draw the thumb (draggable white circle)
        canvas.drawCircle(thumbX, thumbY, 33f, paintThumb)

    }

    private fun updateProgress(x: Float, y: Float) {
//        val cx = width / 2f
//        val cy = height / 2f
//
//        // Calculate the angle of the touch event relative to the center
//        val angle = Math.toDegrees(atan2((y - cy).toDouble(), (x - cx).toDouble())).toFloat()
//
//        // Convert the angle to a value in the range of [0, 360] (starting from top)
//        val adjustedAngle = (angle + 360) % 360
//
//        // Calculate the progressAngle from -90 degrees (starting at top)
//        progressAngle = (adjustedAngle - 90 + 360) % 360
//
//        // Update the progress (hours) based on the progressAngle
//        progress = ((progressAngle / 360) * maxHours).toInt()
//
//        // Ensure progress stays within valid hours (0 to maxHours - 1)
//        progress = max(0, min(progress, maxHours - 1))
//        updateCenterText()
//        invalidate()

        //this is a second approach solution


//        val cx = width / 2f
//        val cy = height / 2f
//
//        // Calculate the angle of the touch event relative to the center
//        val angle = Math.toDegrees(atan2((y - cy).toDouble(), (x - cx).toDouble())).toFloat()
//
//        // Convert the angle to a value in the range of [0, 360] (starting from top)
//        val adjustedAngle = (angle + 360) % 360
//
//        // Calculate the progressAngle from -90 degrees (starting at top)
//        progressAngle = (adjustedAngle - 90 + 360) % 360
//
//        // Update the progress (hours) based on the progressAngle
//        progress = ((progressAngle / 360) * maxHours).toInt()
//        progress = progress.coerceIn(0, maxHours) // Ensure progress stays within valid hours
//        updateCenterText()
//        invalidate()

        //this is a third approach solution
        val cx = width / 2f
        val cy = height / 2f

        // Calculate the angle of the touch event relative to the center
        val angle = Math.toDegrees(atan2((y - cy).toDouble(), (x - cx).toDouble())).toFloat()

        // Convert the angle to a value in the range of [0, 360] (starting from top)
        val adjustedAngle = (angle + 360) % 360

        // Calculate the progressAngle from -90 degrees (starting at top)
        progressAngle = (adjustedAngle - 90 + 360) % 360

        // Update the progress (hours) based on the new progressAngle
        progress = ((progressAngle / 360) * maxHours).toInt()

        // Ensure progress stays within valid hours (0 to maxHours - 1)
        progress = progress.coerceIn(0, maxHours - 1)

        // If progress equals maxHours, set it to 0 (to represent 24-hour cycle)
        if (progress == maxHours) {
            progress = 0
        }

        updateCenterText()

        invalidate()

    }

    private fun isTouchOnThumb(x: Float, y: Float): Boolean {
        val thumbRadius = 40f // Threshold around thumb center for detecting touch
        val distance = sqrt((x - thumbX).pow(2) + (y - thumbY).pow(2))
        return distance <= thumbRadius
    }

    // Snap the thumb to the nearest dot when a dot is tapped
    private fun snapToNearestDot(x: Float, y: Float) {
        var nearestDotIndex = 1
        var nearestDistance = Float.MAX_VALUE

        for (i in hourDots.indices) {
            val (dotX, dotY) = hourDots[i]
            val distance = sqrt((x - dotX).pow(2) + (y - dotY).pow(2))

            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestDotIndex = i
            }
        }

        // Snap the progress to the nearest dot's hour
        progress = nearestDotIndex
        progressAngle = (progress * 360f / maxHours) // Update progressAngle based on the dot
        updateCenterText()
        invalidate()
    }

    private fun updateCenterText() {
        val hoursTextView = (parent as ViewGroup).findViewById<TextView>(R.id.hoursTextView)
        hoursTextView.text = "$progress"
    }

    private fun getAngleFromTouch(x: Float, y: Float): Float {
        val cx = width / 2f
        val cy = height / 2f
        val angle = Math.toDegrees(atan2((y - cy).toDouble(), (x - cx).toDouble())).toFloat()
        return (angle + 360) % 360 // Normalize to [0, 360]
    }

}
