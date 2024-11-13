package com.yesitlab.zyvo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.util.Log
import android.view.View

class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{



    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var maxProgress: Double = 100.0
    private var currentProgress: Double = 0.0
    private var strokeWidthDefault = 2f
    private var radius: Float = 10f  // Default radius (adjust as needed)

    init {
        // Set default paint properties for background
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeCap = Paint.Cap.ROUND
        backgroundPaint.color = Color.LTGRAY
        backgroundPaint.strokeWidth = strokeWidthDefault

        // Set default paint properties for progress
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.strokeWidth = strokeWidthDefault
      //  progressPaint.color = Color.parseColor("#194374")

        // Set the multi-color gradient
        setProgressMultiColorGradient()
    }

    private fun setProgressMultiColorGradient() {
        // Set a SweepGradient with multiple colors
        val colors = intArrayOf(
            Color.parseColor("#99C817"),
            Color.parseColor("#FDEB48"),
            Color.parseColor("#FED137"),
            Color.parseColor("#F7B11E"),
            Color.parseColor("#D72626")
        )

        val positions = floatArrayOf(
            0.0f,   // Start with green
            0.25f,  // Light yellow around 24% progress
            0.40f,  // Yellow around 39% progress
            0.58f,  // Orange around 68% progress
            1.0f    // Red at the end of the progress (100%)
        )
//
//        val sweepGradient = SweepGradient(
//            width / 2f, height.toFloat(),
//            colors, positions
//        )
//        progressPaint.shader = sweepGradient
        val shader = LinearGradient(
            0f, 0f, width.toFloat(), (height * 2).toFloat(),
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        progressPaint.shader = shader
    }
    fun setMax(max: Double) {
        if (max > 0) {
            maxProgress = max
            invalidate()
        }
    }

    fun setProgress(progress: Double) {
        if (progress in 0.0..maxProgress) {
            currentProgress = progress
            invalidate()
        }
    }


    fun setProgressWidth(width: Float) {
        backgroundPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        invalidate()
    }


    fun setProgressWithAnimation(progress: Double, duration: Long = 500) {
        if (progress in 0.0..maxProgress) {
            val animator = ValueAnimator.ofFloat(currentProgress.toFloat(), progress.toFloat())
            animator.duration = duration
            animator.addUpdateListener { animation ->
                currentProgress = animation.animatedValue.toString().toDouble()
                invalidate()
            }
            animator.start()
        } else { currentProgress = maxProgress }


    }

    fun setStrokeWidth(width: Float) {
        backgroundPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        strokeWidthDefault = width
        invalidate()
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    fun setProgressGradient(startColor: Int, endColor: Int) {
        val shader = LinearGradient(
            0f, 0f, width.toFloat(), (height * 2).toFloat(),
            startColor, endColor, Shader.TileMode.CLAMP
        )
        progressPaint.shader = shader
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val strokeWidth = progressPaint.strokeWidth
        val halfStrokeWidth = strokeWidth / 2f

        // Use radius to define the bounds of the arc
        val rect = RectF(


            -35f,  // Adjust the left coordinate
         //   halfStrokeWidth + radius,  // Adjust the top coordinate
            25f,
            1115f,
           // width - halfStrokeWidth - radius, // Adjust the right coordinate
            height * 2 - halfStrokeWidth - radius // Adjust the bottom coordinate
        )

        var left = halfStrokeWidth + radius
        var top = halfStrokeWidth + radius
        var right = width - halfStrokeWidth - radius
        var bottom = height * 2 - halfStrokeWidth - radius

        Log.d("left",left.toString())
        Log.d("top",top.toString())
        Log.d("right",right.toString())
        Log.d("bottom",bottom.toString())


        // Draw the complete background semicircle
        canvas.drawArc(rect, 200f, 140f, false, backgroundPaint)

        // Calculate the angle based on the progress
        val angle = 140f * (currentProgress / maxProgress.toFloat())

        // Draw the progress on top of the background
        canvas.drawArc(rect, 200f, angle.toFloat(), false, progressPaint)
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update gradient on size change to recalculate shader positions
        setProgressMultiColorGradient()
    }
}

