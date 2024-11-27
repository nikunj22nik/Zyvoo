package com.yesitlab.zyvo

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect

class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

    private val outerBorderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val transparentBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var maxProgress: Double = 100.0
    private var currentProgress: Double = 0.0
    private var strokeWidthDefault = 2f
    private var transparentStrokeWidthDefault = 80f
    private var endIcon: Drawable? = null
    private var endIconSize: Int = 40 // Default icon size in dp
    private var radius: Float = 10f  // Default radius (adjust as needed)

    //nikunj code

    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED // Inner semicircle color (red)
        style = Paint.Style.FILL   // Solid fill for the inner semicircle
    }

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF6200EE.toInt() // Outer semicircle color (purple)
        style = Paint.Style.FILL   // Solid fill for the outer semicircle
    }



    init {
        // Initialize the outer border paint
        outerBorderPaint.style = Paint.Style.STROKE
        outerBorderPaint.strokeCap = Paint.Cap.ROUND
        outerBorderPaint.color = Color.TRANSPARENT // Change this to your desired outer border color
        outerBorderPaint.strokeWidth = 60f // Set the desired outer border thickness

        // Set default paint properties for the transparent background
        transparentBackgroundPaint.style = Paint.Style.STROKE
        transparentBackgroundPaint.strokeCap = Paint.Cap.ROUND
        transparentBackgroundPaint.color = Color.RED
        transparentBackgroundPaint.strokeWidth = transparentStrokeWidthDefault

        // Set default paint properties for background
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeCap = Paint.Cap.ROUND
        backgroundPaint.color = Color.LTGRAY
        backgroundPaint.strokeWidth = strokeWidthDefault

        // Set default paint properties for progress
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeCap = Paint.Cap.ROUND
        progressPaint.strokeWidth = strokeWidthDefault

        // Set the multi-color gradient
        setProgressMultiColorGradient()

        // Retrieve custom attributes
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SemiCircleProgressBar, 0, 0)
        endIcon = typedArray.getDrawable(R.styleable.SemiCircleProgressBar_endIcon)
        endIconSize = typedArray.getDimensionPixelSize(R.styleable.SemiCircleProgressBar_endIconSize, dpToPx(40))
        typedArray.recycle()
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
        } else {
            currentProgress = maxProgress
        }
    }

    fun setStrokeWidth(width: Float) {
        backgroundPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        strokeWidthDefault = width
        invalidate()
    }

    fun setEndIcon(drawable: Drawable?) {
        endIcon = drawable
        invalidate()
    }

    fun setEndIconSize(size: Int) {
        endIconSize = size
        invalidate()
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    fun setProgressGradient(startColor: Int, endColor: Int) {
        val shader = LinearGradient(
            0f, 0f, width.toFloat(), (height * 2).toFloat(), startColor, endColor, Shader.TileMode.CLAMP
        )
        progressPaint.shader = shader
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val strokeWidth = progressPaint.strokeWidth
        val halfStrokeWidth = strokeWidth / 2f

        // Adjust the bounds for the outer border arc
        val outerRect = RectF(
            halfStrokeWidth + 20f, // Add padding to prevent clipping
            halfStrokeWidth + 40f,
            width - halfStrokeWidth - 20f,
            height * 2 - halfStrokeWidth - 20f
        )

        // Draw the outer border arc
        canvas.drawArc(outerRect, 200f, 140f, false, outerBorderPaint)

        // Original rect for background and progress

        //nikunj code
//        val rect = RectF(0f, 60f, width.toFloat(), height.toFloat())
//        canvas.drawArc(rect, 200f, 140f, true, transparentBackgroundPaint)

         val rectanglePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF6200EE.toInt() // Rectangle color (e.g., purple)
            style = Paint.Style.FILL
        }

        val rectLeft = 0f
        val rectTop = 60f
        val rectRight = width.toFloat()
        val rectBottom = height.toFloat()



        // Dimensions for the semicircle (aligned to the bottom of the rectangle)
        val semicircleRadius = (rectRight - rectLeft) / 2
        val semicircleCenterX = rectLeft + semicircleRadius
        val semicircleCenterY = rectBottom

        val semicircleRectF = RectF(
            semicircleCenterX - semicircleRadius, // left
            semicircleCenterY - semicircleRadius, // top
            semicircleCenterX + semicircleRadius, // right
            semicircleCenterY  // bottom
        )

        // Draw the semicircle (starting at 180° and sweeping 180°)
        canvas.drawArc(semicircleRectF, 180f, 0f, true, innerPaint)




      //  canvas.drawArc(innerRectF, -180f, 270f, false, innerPaint)

        val middleRect = RectF(
            halfStrokeWidth -35, // Add padding to prevent clipping
            halfStrokeWidth + 60f,
            width - halfStrokeWidth + 30,
            height * 2 - halfStrokeWidth - 60f
        )




        // Draw the background semicircle
        canvas.drawArc(middleRect, 200f, 140f, false, backgroundPaint)

        // Calculate the angle based on the progress
        val angle = 140f * (currentProgress / maxProgress.toFloat())

        // Draw the progress arc
        canvas.drawArc(middleRect, 200f, angle.toFloat(), false, progressPaint)

        // Draw the end icon at the current progress
        endIcon?.let { icon ->
            val angleRadians = Math.toRadians(200.0 + angle) // 200 degrees start + progress
            val centerX = (middleRect.centerX() + middleRect.width() / 2 * Math.cos(angleRadians)).toFloat()
            val centerY = (middleRect.centerY() + middleRect.height() / 2 * Math.sin(angleRadians)).toFloat()

            val halfSize = endIconSize / 2
            val iconRect = Rect(
                (centerX - halfSize).toInt(),
                (centerY - halfSize).toInt() ,
                (centerX + halfSize).toInt(),
                (centerY + halfSize).toInt()
            )
            icon.bounds = iconRect
            icon.draw(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update gradient on size change to recalculate shader positions
        setProgressMultiColorGradient()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        // Perform additional setup if needed after child views are added
    }
}


//21- nov

/*
class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val transparentBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var maxProgress: Double = 100.0
    private var currentProgress: Double = 0.0
    private var strokeWidthDefault = 2f
    private var trasParentStrokeWidthDefault = 80f
    private var endIcon: Drawable? = null
    private var endIconSize: Int = 40 // Default icon size in dp
    private var radius: Float = 10f  // Default radius (adjust as needed)

    init {

        transparentBackgroundPaint.style = Paint.Style.STROKE
        transparentBackgroundPaint.strokeCap = Paint.Cap.ROUND
        transparentBackgroundPaint.color = Color.TRANSPARENT
        transparentBackgroundPaint.strokeWidth = trasParentStrokeWidthDefault

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

        // Retrieve custom attributes
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SemiCircleProgressBar, 0, 0)
        endIcon = typedArray.getDrawable(R.styleable.SemiCircleProgressBar_endIcon)
        endIconSize = typedArray.getDimensionPixelSize(R.styleable.SemiCircleProgressBar_endIconSize, dpToPx(40))
        typedArray.recycle()
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
        fun setEndIcon(drawable: Drawable?) {
        endIcon = drawable
        invalidate()
    }

    fun setEndIconSize(size: Int) {
        endIconSize = size
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
//    private fun renderBorder(canvas: Canvas) {
//         var indicatorBorderRect = RectF()
//
//        canvas.drawArc(
//            indicatorBorderRect,
//            200f,
//            140f,
//            false,
//            transparentBackgroundPaint
//        )}
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
      //  renderBorder(canvas)
        val strokeWidth = progressPaint.strokeWidth
        val halfStrokeWidth = strokeWidth / 2f
        // Use radius to define the bounds of the arc
        val rect = RectF(
            -30f,  // Adjust the left coordinate
         //   halfStrokeWidth + radius,  // Adjust the top coordinate
            20f,
            width.toFloat() + 25f,
            //1115f,
           // width - halfStrokeWidth - radius, // Adjust the right coordinate
            height * 2 - halfStrokeWidth - radius -10f // Adjust the bottom coordinate
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




         // Draw the end icon at the current progress
        endIcon?.let { icon ->
            val angleRadians = Math.toRadians(200.0 + angle) // 200 degrees start + progress
            val centerX = (rect.centerX() + rect.width() / 2 * Math.cos(angleRadians)).toFloat()
            val centerY = (rect.centerY() + rect.height() / 2 * Math.sin(angleRadians)).toFloat()

            val halfSize = endIconSize / 2
            val iconRect = Rect(
                (centerX - halfSize).toInt(),
                (centerY - halfSize).toInt(),
                (centerX + halfSize).toInt(),
                (centerY + halfSize).toInt()
            )
            icon.bounds = iconRect
            icon.draw(canvas)
        }


    }



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update gradient on size change to recalculate shader positions
        setProgressMultiColorGradient()
    }
       private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    override fun onFinishInflate() {
        super.onFinishInflate()
        // Perform additional setup if needed after child views are added
    }
}

 */












/*
class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var maxProgress: Double = 100.0
    private var currentProgress: Double = 0.0
    private var strokeWidthDefault = 2f
    private var radius: Float = 10f  // Default radius (adjust as needed)

    private var endIcon: Drawable? = null
    private var endIconSize: Int = 40 // Default icon size in dp

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

        // Set the multi-color gradient
        setProgressMultiColorGradient()

        // Retrieve custom attributes
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SemiCircleProgressBar, 0, 0)
        endIcon = typedArray.getDrawable(R.styleable.SemiCircleProgressBar_endIcon)
        endIconSize = typedArray.getDimensionPixelSize(R.styleable.SemiCircleProgressBar_endIconSize, dpToPx(40))
        typedArray.recycle()
    }

    private fun setProgressMultiColorGradient() {
        val colors = intArrayOf(
            Color.parseColor("#99C817"),
            Color.parseColor("#FDEB48"),
            Color.parseColor("#FED137"),
            Color.parseColor("#F7B11E"),
            Color.parseColor("#D72626")
        )
        val positions = floatArrayOf(0.0f, 0.25f, 0.40f, 0.58f, 1.0f)

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

    fun setEndIcon(drawable: Drawable?) {
        endIcon = drawable
        invalidate()
    }

    fun setEndIconSize(size: Int) {
        endIconSize = size
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val strokeWidth = progressPaint.strokeWidth
        val halfStrokeWidth = strokeWidth / 2f

        // Define arc bounds
        val rect = RectF(
            -30f,  // Adjust the left coordinate
            20f,
            width.toFloat() + 25f,
            height * 2 - halfStrokeWidth - radius - 10f
        )

        // Draw the complete background semicircle
        canvas.drawArc(rect, 200f, 140f, false, backgroundPaint)

        // Calculate the angle based on the progress
        val angle = 140f * (currentProgress / maxProgress.toFloat())

        // Draw the progress on top of the background
        canvas.drawArc(rect, 200f, angle.toFloat(), false, progressPaint)

        // Draw the end icon at the current progress
        endIcon?.let { icon ->
            val angleRadians = Math.toRadians(200.0 + angle) // 200 degrees start + progress
            val centerX = (rect.centerX() + rect.width() / 2 * Math.cos(angleRadians)).toFloat()
            val centerY = (rect.centerY() + rect.height() / 2 * Math.sin(angleRadians)).toFloat()

            val halfSize = endIconSize / 2
            val iconRect = Rect(
                (centerX - halfSize).toInt(),
                (centerY - halfSize).toInt(),
                (centerX + halfSize).toInt(),
                (centerY + halfSize).toInt()
            )
            icon.bounds = iconRect
            icon.draw(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setProgressMultiColorGradient()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}













 */