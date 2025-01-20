package com.business.zyvo

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
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class CircularSeekBar1 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{
    companion object {
        private const val MAX_SCALE = 11
        private const val MIX_SCALE = 0

        private const val MAJOR_TICK_SIZE = 80f
        private const val MINOR_TICK_SIZE = 50f
        private const val MAJOR_TICK_WIDTH = 6f
        private const val MINOR_TICK_WIDTH = 3f
        private const val TICK_MARGIN = 30f
        private const val TICK_TEXT_MARGIN = 15f

        private const val MIN_ANGLE = 90f
        private const val MAX_ANGLE = -240f
        private const val START_ANGLE = -90f
        private const val SWEEP_ANGLE = 360f
    }

    private val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4AEAB1")
        strokeWidth = 5f
        style = Paint.Style.FILL
    }

    @Dimension
    private var _borderWidth = 100f
        set(value) {
            field = value
            _iconWidth = value / 2
            indicatorBorderPaint.strokeWidth = value
            indicatorFillPaint.strokeWidth = value
        }

    @Dimension
    private var _iconWidth: Float = _borderWidth / 2

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
        strokeWidth = _borderWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val indicatorFillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = _fillColor
        strokeWidth = _borderWidth
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

    private var is24HR = false
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

        indicatorBorderRect.set(
            _borderWidth / 2,
            _borderWidth / 2,
            height.coerceAtMost(width) - _borderWidth / 2,
            height.coerceAtMost(width) - _borderWidth / 2
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = MeasureSpec.getSize(heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)

        radius = (height.coerceAtMost(width) - _borderWidth) / 2

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



    /*
        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action and event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
    //                if (inStartCircleButton(event.x, event.y) && isEnabled && isStartEnabled) {
    //                    isInStartIcon = true
    //                    preRadian = getRadian(event.x, event.y)
    //
    //                    parent.requestDisallowInterceptTouchEvent(true)
    //                    return true
    //                } else
                    if (inEndCircleButton(event.x, event.y) && isEnabled && isEndEnabled) {
                        isInEndIcon = true
                        preRadian = getRadian(event.x, event.y)

                        parent.requestDisallowInterceptTouchEvent(true)
                        updateCenterText()
                        return true
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isInStartIcon && isEnabled && isStartEnabled) {
                        val tempRadian = getRadian(event.x, event.y)
                        currentStartRadian = getCurrentRadian(currentStartRadian, tempRadian)

                        startHours = radianToHours(currentStartRadian)
                        return true
                    } else if (isInEndIcon && isEnabled && isEndEnabled) {
                        val tempRadian = getRadian(event.x, event.y)
                        currentEndRadian = getCurrentRadian(currentEndRadian, tempRadian)

                        endHours = radianToHours(currentEndRadian)
                        return true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (isInStartIcon) {
                        isInStartIcon = false

                        timeChangedListener?.onStartChanged(
                            startHours.toInt(),
                            getHoursMinute(startHours)
                        )
                        return true
                    } else if (isInEndIcon) {
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

     */



    /*
override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.action and event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            // Check if the touch is near any dot
            val dotIndex = getClickedDotIndex(event.x, event.y)
            if (dotIndex != -1) {
                // Map dot index to hours
                val hour = mapDotIndexToHour(dotIndex)
                onDotClick(hour)
                return true
            }
        }
        MotionEvent.ACTION_MOVE -> {
            // Existing logic for dragging the end indicator
            if (isInEndIcon && isEnabled && isEndEnabled) {
                val tempRadian = getRadian(event.x, event.y)
                currentEndRadian = getCurrentRadian(currentEndRadian, tempRadian)

                endHours = radianToHours(currentEndRadian)
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

     */



    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Check if the touch is near any dot
                val dotIndex = getClickedDotIndex(event.x, event.y)
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
                if (isInEndIcon && isEnabled && isEndEnabled) {
                    val tempRadian = getRadian(event.x, event.y)
                    currentEndRadian = getCurrentRadian(currentEndRadian, tempRadian)

                    endHours = radianToHours(currentEndRadian)
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


    private fun getClickedDotIndex(x: Float, y: Float): Int {
        val dotCount = 12
        val clickRadius = 30f
        for (i in 0 until dotCount) {
            val angle = Math.toRadians(START_ANGLE + i * (SWEEP_ANGLE / dotCount).toDouble())
            val dotX = centerX + radius * cos(angle)
            val dotY = centerY + radius * sin(angle)
            if (sqrt((x - dotX).pow(2) + (y - dotY).pow(2)) <= clickRadius) {
                // If the clicked index is 0, return 12 instead.
                return  i
            }
            Log.d("timeVipin", i.toString())
        }
        return -1


    }




    /*
    private fun getClickedDotIndex(x: Float, y: Float): Int {
        val dotCount = 12 // Number of dots (12 hours or customized)
        val clickRadius = 40f // Adjust this value for better sensitivity

        for (i in 0 until dotCount) {
            // Calculate each dot's position
            val angle = Math.toRadians(START_ANGLE + i * (SWEEP_ANGLE / dotCount).toDouble())
            val dotX = centerX + radius * cos(angle)
            val dotY = centerY + radius * sin(angle)

            // Check if the touch is within the proximity of the dot
            if (sqrt((x - dotX).pow(2) + (y - dotY).pow(2)) <= clickRadius) {
                return i
            }
        }
        return -1
    }

     */



    private fun isTouchNearDot(touchX: Float, touchY: Float, dotX: Float, dotY: Float): Boolean {
        val touchRadius = 40f // Allowable distance to consider the touch on a dot
        val dx = touchX - dotX
        val dy = touchY - dotY
        return sqrt(dx * dx + dy * dy) <= touchRadius
    }

    private fun mapDotIndexToHour(dotIndex: Int): Float {
        return if (is24HR) dotIndex * 2f else dotIndex.toFloat()
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

    fun setOnTimeChangedListener(listener: OnTimeChangedListener) {
        timeChangedListener = listener
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
//            _startIconResource = it.getResourceId(
//                R.styleable.ClockSlider_cc_startIconResource,
//                _startIconResource
//            )
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

    private fun renderBorder(canvas: Canvas) {


        canvas.drawArc(
            indicatorBorderRect,
            START_ANGLE,
            SWEEP_ANGLE,
            false,
            indicatorBorderPaint
        )


        val dotCount = 12 // Number of dots
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
//        startIcon?.apply {
//            val hour = if (is24HR) startHours / 2 else startHours
//
//            setBounds(
//                (centerX - _iconWidth + (centerX - _borderWidth / 2) * cos(mapTextToAngle(hour).toRadian())).toInt(),
//                (centerY - _iconWidth - (centerY - _borderWidth / 2) * sin(mapTextToAngle(hour).toRadian())).toInt(),
//                (centerX + _iconWidth + (centerX - _borderWidth / 2) * cos(mapTextToAngle(hour).toRadian())).toInt(),
//                (centerY + _iconWidth - (centerY - _borderWidth / 2) * sin(mapTextToAngle(hour).toRadian())).toInt()
//            )
//            draw(canvas)

        getHoursMinute(startHours)
        // }
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
        return  if (timeHour.equals("0")) "12" else  timeHour
    }


    private fun updateCenterText() {
        val hoursTextView = (parent as ViewGroup).findViewById<TextView>(R.id.textTime)
        hoursTextView.text = getTimeText()

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
}