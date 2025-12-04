package com.business.zyvo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class JustifiedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val justifiedTextPaint: TextPaint = paint

    @SuppressLint("WrongConstant")
    override fun onDraw(canvas: Canvas) {
        val textContent = text?.toString() ?: return
        val width = measuredWidth - paddingLeft - paddingRight

        val staticLayout = StaticLayout.Builder.obtain(
            textContent, 0, textContent.length, justifiedTextPaint, width
        ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
            .setIncludePad(true)
            .setBreakStrategy(Layout.BREAK_STRATEGY_BALANCED)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL)
            .build()

        canvas?.save()
        canvas?.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        staticLayout.draw(canvas)
        canvas?.restore()
    }
}
