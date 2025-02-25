package com.business.zyvo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class ScheduleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val gridPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private var daysOfWeek: List<String> = listOf()
    private var events: List<ScheduleEvent> = listOf()

    private val timeColumnWidth = 250f
    private val dayColumnWidth = 370f
    private val daySpacing = 20f
    private val rowHeight = 250f
    private val rowSpacing = 20f
    private val headerHeight = rowHeight
    //private var totalWidth = (dayColumnWidth * 7)
    private  var day : Int = 0
    private val totalHeight = headerHeight + (rowHeight  * 24) // 24 time slots
    fun setDays(days: List<String>) {
        this.daysOfWeek = days
        day = days.size
        requestLayout()
        invalidate() // Redraw the view
    }
    private var totalWidth = (dayColumnWidth * day)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = ((dayColumnWidth + daySpacing) * day).toInt()
        val height = (headerHeight + (rowHeight  * 27)).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 55f
            color = Color.parseColor("#1E1E46")
            textAlign = Paint.Align.CENTER
        }

        // **Draw Weekday Labels**
        for (i in daysOfWeek.indices) {
            val x = (dayColumnWidth + daySpacing) * i
            val y = 34f
            val right = x + dayColumnWidth
            val bottom = headerHeight

            val drawable = ContextCompat.getDrawable(context, R.drawable.bg_four_side_corner_inner_color_filled)
            drawable?.setBounds(x.toInt(), y.toInt(), right.toInt(), bottom.toInt())
            drawable?.draw(canvas)

            canvas.drawText(daysOfWeek[i], x + (dayColumnWidth / 2), headerHeight / 2 + 20, textPaint)
        }

        // **Loop Through Grid and Draw Empty Cells**
        for (col in 0 until daysOfWeek.size) {
            for (row in 0 until 24) { // 24 time slots
                val isCellOccupied = events.any { it.column == col && it.row == row }
                if (!isCellOccupied) {
                    drawEmptyCellPlaceholder(canvas, col, row)
                }
            }
        }

        // **Draw Events**
        for (event in events) {
            drawEvent(canvas, event)
        }
    }



    fun setEvents(eventList: List<ScheduleEvent>) {
        this.events = eventList
        invalidate()
    }

    private fun drawEvent(canvas: Canvas, event: ScheduleEvent) {
        val paddingTopBottom = 15
//        val left = (dayColumnWidth + daySpacing) * event.column + 10
//        val top = headerHeight + (rowHeight * event.row) + 40
//        val right = left + dayColumnWidth - 20
//        val bottom = top +  rowHeight - 18

        val left = (dayColumnWidth + daySpacing) * event.column + 10
        val top = headerHeight + ((rowHeight + 30) * event.row) + 40 + paddingTopBottom
        val right = left + dayColumnWidth - 20
        val bottom = top + (rowHeight + 15)- 20 - (2 * (paddingTopBottom - 5 ))

        val drawable = ContextCompat.getDrawable(context, event.drawableRes)
        drawable?.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        drawable?.draw(canvas)

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
        }
        val maxTextWidth = dayColumnWidth - 80 // Adjust for padding
        val limitedTitle = getLimitedText(event.title, 9, titlePaint, maxTextWidth)
        canvas.drawText(limitedTitle, left + 40, top + 50, titlePaint)

        val subtitlePaint = Paint().apply {
            if (event.subtitle == "confirmed") {
                color = Color.parseColor("#4AEAB1")
            }else if (event.subtitle == "pending"){
                color = Color.parseColor("#F5A43D")
            }else{
                color = Color.parseColor("#F5A43D")
            }
            textSize = 38f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(event.subtitle, left + 40, top + 110, subtitlePaint)

        val timePaint = Paint().apply {
            color = Color.BLACK
            textSize = 33f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(event.time, left + 40, top + 170, timePaint)
    }

    fun getLimitedText(text: String?, maxWords: Int, paint: Paint, maxWidth: Float): String {
        if (text.isNullOrBlank()) return ""

        val words = text.split(" ")
        var limitedText = words.take(maxWords).joinToString(" ")

        // Ensure text fits within the available width
        while (paint.measureText(limitedText) > maxWidth && limitedText.contains(" ")) {
            limitedText = limitedText.substringBeforeLast(" ") + "..."
        }

        return limitedText
    }

    private fun drawEmptyCellPlaceholder(canvas: Canvas, column: Int, row: Int) {
        val paddingTopBottom = 15 // Adjust this value for more or less spacing

        val left = (dayColumnWidth + daySpacing) * column + 10
        val top = headerHeight + ((rowHeight + 30) * row) + 40 + paddingTopBottom
        val right = left + dayColumnWidth - 20
        val bottom = top + (rowHeight + 15)- 20 - (2 * (paddingTopBottom - 5 )) // Subtracting twice to balance padding

        val placeholderDrawable = ContextCompat.getDrawable(context, R.drawable.empty_event_icon)
        placeholderDrawable?.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        placeholderDrawable?.draw(canvas)
    }

}


