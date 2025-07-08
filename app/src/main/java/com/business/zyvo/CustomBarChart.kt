package com.business.zyvo

//import android.content.Context
//import android.graphics.RectF
//import android.util.AttributeSet
//import android.util.Log
//import com.github.mikephil.charting.charts.BarLineChartBase
//import com.github.mikephil.charting.components.YAxis.AxisDependency
//import com.github.mikephil.charting.data.BarData
//import com.github.mikephil.charting.data.BarEntry
//import com.github.mikephil.charting.highlight.BarHighlighter
//import com.github.mikephil.charting.highlight.Highlight
//import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
//import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
//import com.github.mikephil.charting.renderer.BarChartRenderer
//
//open class CustomBarChart : BarLineChartBase<BarData>, BarDataProvider {
//
//    // Use `var` for mutable properties
//    var highlightFullBarEnabled: Boolean = false
//    var drawValueAboveBar: Boolean = true
//    var drawBarShadow: Boolean = false
//    var fitBars: Boolean = false
//
//    constructor(context: Context?) : super(context)
//    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
//    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
//
//    override fun init() {
//        super.init()
//
//        // Use setters instead of direct assignment
//        renderer = BarChartRenderer(this, animator, viewPortHandler)
//        highlighter = BarHighlighter(this)
//
//        xAxis.spaceMin = 0.5f
//        xAxis.spaceMax = 0.5f
//    }
//
//    override fun calcMinMax() {
//        data?.let { barData ->
//            if (fitBars) {
//                xAxis.calculate(
//                    barData.xMin - barData.barWidth / 2f,
//                    barData.xMax + barData.barWidth / 2f
//                )
//            } else {
//                xAxis.calculate(barData.xMin, barData.xMax)
//            }
//
//            axisLeft.calculate(
//                barData.getYMin(AxisDependency.LEFT),
//                barData.getYMax(AxisDependency.LEFT)
//            )
//            axisRight.calculate(
//                barData.getYMin(AxisDependency.RIGHT),
//                barData.getYMax(AxisDependency.RIGHT)
//            )
//        }
//    }
//
//    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
//        if (data == null) {
//            Log.e("MPAndroidChart", "Can't select by touch. No data set.")
//            return null
//        }
//
//        return highlighter.getHighlight(x, y)?.let { h ->
//            if (highlightFullBarEnabled) {
//                Highlight(h.x, h.y, h.xPx, h.yPx, h.dataSetIndex, -1, h.axis)
//            } else {
//                h
//            }
//        }
//    }
//
//    fun getBarBounds(e: BarEntry): RectF {
//        val bounds = RectF()
//        getBarBounds(e, bounds)
//        return bounds
//    }
//
//    fun getBarBounds(e: BarEntry, outputRect: RectF) {
//        data?.getDataSetForEntry(e)?.let { set ->
//            val y = e.y
//            val x = e.x
//            val barWidth = data.barWidth
//            val left = x - barWidth / 2f
//            val right = x + barWidth / 2f
//            val top = if (y >= 0f) y else 0f
//            val bottom = if (y <= 0f) y else 0f
//
//            outputRect.set(left, top, right, bottom)
//            getTransformer(set.axisDependency).rectValueToPixel(outputRect)
//        } ?: outputRect.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)
//    }
//
//    override fun isDrawValueAboveBarEnabled() = drawValueAboveBar
//    override fun isDrawBarShadowEnabled() = drawBarShadow
//    override fun isHighlightFullBarEnabled() = highlightFullBarEnabled
//    override fun getBarData(): BarData = data ?: BarData()
//}