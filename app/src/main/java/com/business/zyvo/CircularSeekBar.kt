package com.business.zyvo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import com.business.zyvo.utils.ErrorDialog
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class CircularSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{
    companion object {
        private const val MAX_SCALE = 11
        private const val MIX_SCALE = 0
        private const val MAJOR_TICK_WIDTH = 6f
        private const val MINOR_TICK_WIDTH = 3f
        private const val MIN_ANGLE = 90f
        private const val MAX_ANGLE = -240f
        private const val START_ANGLE = -90f
        private const val SWEEP_ANGLE = 360f

    }

    // Define the listener interface
    interface OnSeekBarChangeListener {
        fun onProgressChanged(progress: String)
    }
    // The listener object that will be passed from your activity or fragment
    private var listener: OnSeekBarChangeListener? = null

    // Setting the listener
    fun setOnSeekBarChangeListener(listener: OnSeekBarChangeListener) {
        this.listener = listener
    }

    private val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4AEAB1")
        strokeWidth = 5f
        style = Paint.Style.FILL
    }

    @Dimension
    private var _borderWidth: Float = 110f
        set(value) {
            field = value
            _iconWidth = value / 2
            indicatorBorderPaint.strokeWidth = value
            indicatorFillPaint.strokeWidth = value
            invalidate() // Refresh the view after changing border width
        }


    @Dimension
    private var _iconWidth: Float = _borderWidth.toFloat() / 2

    @Dimension
    private var _metricTextSizeWidth: Float = 130f
        set(value) {
            field = value
            metricPaint.textSize = value
        }

    @ColorInt

    private var _borderColor = Color.WHITE
        set(value) {
            field = value
            indicatorBorderPaint.color = value
            majorTickPaint.color = value
            minorTickPaint.color = value
        }

    @ColorInt
    private var _fillColor = Color.YELLOW
        set(value) {
            field = value
            indicatorFillPaint.color = value
            metricPaint.color = value
        }

    @ColorInt
    private var _tickTextColor = Color.BLACK
        set(value) {
            field = value
            tickTextPaint.color = value
        }



    @DrawableRes
    private var _endIconResource: Int = android.R.drawable.btn_star_big_off
        set(value) {
            field = value
            endIcon = ResourcesCompat.getDrawable(resources, value, context.theme)
        }

    private var metricMode: MetricMode = MetricMode.COUNTER

    private var indicatorBorderRect = RectF()

    private val textBoundsRect = Rect()

    private val indicatorBorderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = _borderColor
        strokeWidth = _borderWidth.toFloat()
        strokeCap = Paint.Cap.ROUND
    }

    private val indicatorFillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = _fillColor
        strokeWidth = _borderWidth.toFloat()
        strokeCap = Paint.Cap.ROUND
    }

    private val majorTickPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = _borderColor
        strokeWidth = MAJOR_TICK_WIDTH
        strokeCap = Paint.Cap.BUTT
    }

    private val minorTickPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = _borderColor
        strokeWidth = MINOR_TICK_WIDTH
        strokeCap = Paint.Cap.BUTT
    }

    private val tickTextPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = _tickTextColor
        textSize = 40f
    }

    private val metricPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = _fillColor
        textSize = _metricTextSizeWidth
    }


    private var endIcon: Drawable? = null

    private val decimalFormat = DecimalFormat("##0")
    // private val decimalFormat = DecimalFormat("##0hr")

    private val centerX get() = width / 2f
    private val centerY get() = height / 2f

    private var isInStartIcon: Boolean = false
    private var isInEndIcon: Boolean = false

    private var currentStartRadian: Float = 0f
    private var currentEndRadian: Float = 0f
    private var radius: Float = 0f
    private var preRadian: Float = 0f

    private var _angleOfAnHour = 30

    var isStartEnabled: Boolean = true
    var isEndEnabled: Boolean = true

    private var is24HR = true
        set(value) {
            field = value
            _angleOfAnHour = if (is24HR) 15 else 30
        }

    var startHours: Float = 0f
        set(value) {
            field = if (is24HR) value % 24 else value % 12
            currentStartRadian = hoursToAdian(field)
            //if (is24HR) value % 24 else value % 12
//            currentStartRadian = hoursToAdian(field)
//            invalidate()
        }

    var endHours: Float = 0f
        set(value) {
          //  field = value.coerceIn(0f, 24f)
            field = if (is24HR) value % 24 else value % 12
            currentEndRadian = hoursToAdian(field)
            invalidate()
        }

    interface OnTimeChangedListener {
        fun onStartChanged(hour: Int, minute: Int)

        fun onEndChanged(hour: Int, minute: Int)
    }

    private var timeChangedListener: OnTimeChangedListener? = null

    init {
        decimalFormat.roundingMode = RoundingMode.HALF_UP

        obtainStyledAttributes(attrs, defStyleAttr)


    }


override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    validateBorderWidth(w, h) // Ensure border width is valid
    indicatorBorderRect.set(
        _borderWidth / 2,
        _borderWidth / 2,
        w - _borderWidth / 2,
        h - _borderWidth / 2
    )
}


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = MeasureSpec.getSize(heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)

        radius = (height.coerceAtMost(width) - _borderWidth.toFloat()) / 2

        setMeasuredDimension(height.coerceAtMost(width), height.coerceAtMost(width))
    }

    override fun onDraw(canvas: Canvas) {
        renderBorder(canvas)
        renderBorderFill(canvas)
        renderBorderStart(canvas)
        renderBorderEnd(canvas)

        //  renderMajorTicks(canvas)
        // renderMinorTicks(canvas)

        renderPeriodText(canvas)

//renderCurveBorder(canvas)

    }

    fun rotateToDot(dotIndex: Int) {
        Log.d("TESTING","i AM HERE IN TOTA");
        val angleOfDot = dotIndex * (360 / 12)  // Assuming 12 dots
        val radian = Math.toRadians(angleOfDot.toDouble()).toFloat()

        // Update either the start or end angle based on your need
        currentStartRadian = radian

        invalidate()  // Redraw the view
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Check if the touch is near any dot
                val dotIndex = getClickedDotIndex(event.x, event.y)
                Log.d(ErrorDialog.TAG,dotIndex.toString())

                if (dotIndex != -1) {
                    // Map dot index to hours
                    val hour = mapDotIndexToHour(dotIndex)
                    onDotClick(hour)
                    return true
                } else if (inEndCircleButton(event.x, event.y) && isEnabled && isEndEnabled) {
                    // Check if the touch is on the end indicator
                    isInEndIcon = true
                    preRadian = getRadian(event.x, event.y)

                    parent.requestDisallowInterceptTouchEvent(true)
                    updateCenterText()
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // Handle dragging logic
//                if (isInEndIcon && isEnabled && isEndEnabled) {
//                    val tempRadian = getRadian(event.x, event.y)
//                    currentEndRadian = getCurrentRadian(currentEndRadian, tempRadian)
//
//                    endHours = radianToHours(currentEndRadian)
//                    return true
//                }

                if (isInEndIcon && isEnabled && isEndEnabled) {
                    val tempRadian = getRadian(event.x, event.y)
                    currentEndRadian = getCurrentRadian(currentEndRadian, tempRadian)

                    // Prevent endHours from exceeding 24 or going below 0
                    val tempEndHours = radianToHours(currentEndRadian)
                    if (tempEndHours in 2f..24f) {
                        endHours = tempEndHours
                    }
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isInEndIcon) {
                    isInEndIcon = false

                    timeChangedListener?.onEndChanged(
                        endHours.toInt(),
                        getHoursMinute(endHours)
                    )
                    return true
                }

                performClick()
            }
        }
        return false
    }

/*
    private fun getClickedDotIndex(x: Float, y: Float): Int {
        val dotCount = 24
        val clickRadius = 50f
        for (i in 0 until dotCount) {
            val angle = Math.toRadians(START_ANGLE + i * (SWEEP_ANGLE / dotCount).toDouble())
            val dotX = centerX + radius * cos(angle)
            val dotY = centerY + radius * sin(angle)
//            if (sqrt((x - dotX).pow(2) + (y - dotY).pow(2)) <= clickRadius) {
//                // If the clicked index is 0, return 12 instead.
//                return  i
//            }
//            Log.d("timeVipin", i.toString())
//        }
//        return -1

            val distance = sqrt((x - dotX).pow(2) + (y - dotY).pow(2))
            Log.d("DotDetection", "Dot $i at ($dotX, $dotY), touch at ($x, $y), distance: $distance")
            if (distance <= clickRadius) {
                return i


            }
        }
        return -1


    }

 */

private fun getClickedDotIndex(x: Float, y: Float): Int {
    val dotCount = 24
    val clickRadius = 20f
    var closestDotIndex = -1
    var smallestDistance = Float.MAX_VALUE
    var closestDotLog = ""

    for (i in 2 until dotCount) {
        val angle = Math.toRadians(START_ANGLE + i * (SWEEP_ANGLE / dotCount).toDouble())
        val dotX = centerX + radius * cos(angle)
        val dotY = centerY + radius * sin(angle)

        val distance = sqrt((x - dotX).pow(2) + (y - dotY).pow(2))
        Log.d("DotDetection", "Dot $i at ($dotX, $dotY), touch at ($x, $y), distance: $distance")

        if (distance < smallestDistance) {
            smallestDistance = distance.toFloat()
            closestDotIndex = i
            closestDotLog = "Dot $i at ($dotX, $dotY), touch at ($x, $y), distance: $distance"
        }
    }

    Log.d("ClosestDot", closestDotLog) // Log only the closest dot
    return if (smallestDistance <= clickRadius) closestDotIndex else -1
}




    @SuppressLint("SuspiciousIndentation")
    private fun mapDotIndexToHour(dotIndex: Int): Float {
     val value = if (is24HR) dotIndex * 1f else dotIndex.toFloat()
        Log.d("VipinValue",value.toString())
        return  value
    }


    private fun onDotClick(hour: Float) {
        endHours = hour
        timeChangedListener?.onEndChanged(
            endHours.toInt(),
            getHoursMinute(endHours)
        )
        invalidate()
    }


    override fun performClick(): Boolean {
        return super.performClick()
    }



    private fun obtainStyledAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircularSeekBar,
            defStyleAttr,
            0
        ).use {
            _borderWidth = it.getDimension(
                R.styleable.CircularSeekBar_cc_borderWidth,
                _borderWidth
            )
            _metricTextSizeWidth = it.getDimension(
                R.styleable.CircularSeekBar_cc_metricTextSize,
                _metricTextSizeWidth
            )
            _borderColor = it.getColor(
                R.styleable.CircularSeekBar_cc_borderColor,
                _borderColor
            )
            _fillColor = it.getColor(
                R.styleable.CircularSeekBar_cc_fillColor,
                _fillColor
            )
            _tickTextColor = it.getColor(
                R.styleable.CircularSeekBar_cc_tickTextColor,
                _tickTextColor
            )
            _endIconResource = it.getResourceId(
                R.styleable.CircularSeekBar_cc_endIconResource,
                _endIconResource
            )
            isStartEnabled = it.getBoolean(R.styleable.CircularSeekBar_cc_isStartEnabled, isStartEnabled)
            isEndEnabled = it.getBoolean(R.styleable.CircularSeekBar_cc_isEndEnabled, isEndEnabled)
            is24HR = it.getBoolean(R.styleable.CircularSeekBar_cc_is24HR, is24HR)
            startHours = it.getFloat(R.styleable.CircularSeekBar_cc_startHour, startHours)
            endHours = it.getFloat(R.styleable.CircularSeekBar_cc_endHour, endHours)
            metricMode = MetricMode.find(
                it.getInt(
                    R.styleable.CircularSeekBar_cc_metricMode,
                    metricMode.ordinal
                )
            )
        }
    }
    private fun validateBorderWidth(maxWidth: Int, maxHeight: Int) {
        val maxDimension = maxWidth.coerceAtMost(maxHeight)
        if (_borderWidth > maxDimension / 2) {
            _borderWidth = maxDimension / 2f
        }
    }


    private fun renderBorder(canvas: Canvas) {
        canvas.drawArc(
            indicatorBorderRect,
            START_ANGLE,
            SWEEP_ANGLE,
            false,
            indicatorBorderPaint
        )


        val dotCount = 24 // Number of dots
        val radius = indicatorBorderRect.width() / 2 // Radius of the circle
        val centerX = indicatorBorderRect.centerX()
        val centerY = indicatorBorderRect.centerY()

        for (i in 0 until dotCount) {
            // Calculate angle for each dot
            val angle = Math.toRadians(START_ANGLE + i * (SWEEP_ANGLE / dotCount).toDouble())

            // Calculate dot position
            val dotX = centerX + radius * cos(angle)
            val dotY = centerY + radius * sin(angle)

            // Draw the dot
            canvas.drawCircle(dotX.toFloat(), dotY.toFloat(), 8f, paintDot)
        }
        updateCenterText()
    }

    private fun renderBorderFill(canvas: Canvas) {
        canvas.drawArc(
            indicatorBorderRect,
            START_ANGLE + (startHours * _angleOfAnHour),
            when {
                endHours >= startHours -> (endHours - startHours) * _angleOfAnHour
                is24HR -> (endHours + 24 - startHours) * _angleOfAnHour
                else -> (endHours + 12 - startHours) * _angleOfAnHour
            },
            false,
            indicatorFillPaint
        )
        updateCenterText()


    }





    private fun renderBorderStart(canvas: Canvas) {
        getHoursMinute(startHours)
    }

    private fun renderBorderEnd(canvas: Canvas) {
        endIcon?.apply {
            val hour = if (is24HR) endHours / 2 else endHours

            setBounds(
                (centerX - _iconWidth + (centerX - _borderWidth / 2) * cos(mapTextToAngle(hour).toRadian())).toInt(),
                (centerY - _iconWidth - (centerY - _borderWidth / 2) * sin(mapTextToAngle(hour).toRadian())).toInt(),
                (centerX + _iconWidth + (centerX - _borderWidth / 2) * cos(mapTextToAngle(hour).toRadian())).toInt(),
                (centerY + _iconWidth - (centerY - _borderWidth / 2) * sin(mapTextToAngle(hour).toRadian())).toInt()
            )

            draw(canvas)
        }
    }




    fun renderPeriodText(canvas: Canvas) {
        val start = startHours.toInt() * 60 + getHoursMinute(startHours)
        val end = endHours.toInt() * 60 + getHoursMinute(endHours)
        val time = when {
            end >= start -> end - start
            is24HR -> end - start + 24 * 60
            else -> end - start + 12 * 60
        }

        val startText = if (startHours.toInt() == 0) 12 else startHours.toInt()
        val timeText = decimalFormat.format(time / 60 + ((time % 60).toFloat() / 100) )


        canvas.drawTextCentred(
            if (metricMode == MetricMode.COUNTER) "$startText \n Hours" else   "$timeText ",
            width / 2f,
            height / 2f,
            metricPaint
        )

    }


    private fun getTimeText(): String {
        val start = startHours.toInt() * 60 + getHoursMinute(startHours)
        val end = endHours.toInt() * 60 + getHoursMinute(endHours)
        val time = when {
            end >= start -> end - start
            is24HR -> end - start + 24 * 60
            else -> end - start + 12 * 60
        }

        var timeHour = decimalFormat.format(time / 60 + ((time % 60).toFloat() / 100))
        return  if (timeHour.equals("0")) "0" else if (timeHour.equals("24")) "23" else  timeHour
    }


    private fun updateCenterText() {
        val hoursTextView = (parent as ViewGroup).findViewById<TextView>(R.id.textTime)
        hoursTextView.text = getTimeText()
        listener?.onProgressChanged(getTimeText())
    }

    private fun mapTextToAngle(time: Float): Float {
        return (MIN_ANGLE + ((MAX_ANGLE - MIN_ANGLE) / (MAX_SCALE - MIX_SCALE)) * (time - MIX_SCALE))
    }



    private fun inEndCircleButton(x: Float, y: Float): Boolean {
        val r: Float = radius - _borderWidth / 2
        val x2 = (centerX + r * sin(currentEndRadian.toDouble())).toFloat()
        val y2 = (centerY - r * cos(currentEndRadian.toDouble())).toFloat()
        return sqrt(((x - x2) * (x - x2) + (y - y2) * (y - y2)).toDouble()) < (_borderWidth)
    }

    private fun getRadian(x: Float, y: Float): Float {
        var alpha = atan(((x - centerX) / (centerY - y)).toDouble()).toFloat()
        if (x > centerX && y > centerY) {
            alpha += Math.PI.toFloat()
        } else if (x < centerX && y > centerY) {
            alpha += Math.PI.toFloat()
        } else if (x < centerX && y < centerY) {
            alpha = (2 * Math.PI + alpha).toFloat()
        }
        return alpha
    }

    private fun getCurrentRadian(currentRadian: Float, tmpRadian: Float): Float {
        var currentRadian = currentRadian
        if (preRadian > Math.toRadians(270.0) && tmpRadian < Math.toRadians(90.0)) {
            preRadian -= (2 * Math.PI).toFloat()
        } else if (preRadian < Math.toRadians(90.0) && tmpRadian > Math.toRadians(270.0)) {
            preRadian = (tmpRadian + (tmpRadian - 2 * Math.PI) - preRadian).toFloat()
        }

        currentRadian += tmpRadian - preRadian
        preRadian = tmpRadian

        if (currentRadian > 2 * Math.PI) {
            currentRadian -= (2 * Math.PI).toFloat()
        }
        if (currentRadian < 0) {
            currentRadian += (2 * Math.PI).toFloat()
        }

        return currentRadian
    }

    private fun radianToHours(radian: Float): Float {
        val degree = Math.toDegrees(radian.toDouble()) % 360
        return (degree / _angleOfAnHour).toFloat()
    }

    private fun hoursToAdian(hours: Float): Float {
        val degree = hours * _angleOfAnHour
        return Math.toRadians(degree.toDouble()).toFloat()
    }

    private fun getHoursMinute(hours: Float): Int {
        var minute = (hours * 60 % 60).toInt()
        val level = 5

        for (i in level..(minute + level) step level) {
            if (minute < i) {
                minute = i - level
                break
            }
        }

        return minute
    }

    private fun Float.toRadian(): Float {
        return this * (PI / 180).toFloat()
    }

    private fun Canvas.drawTextCentred(text: String, cx: Float, cy: Float, paint: Paint) {
        paint.getTextBounds(text, 0, text.length, textBoundsRect)
        drawText(
            text,
            cx - textBoundsRect.exactCenterX(),
            cy - textBoundsRect.exactCenterY(),
            paint
        )
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}

