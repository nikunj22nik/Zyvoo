package com.business.zyvo.fragment.guest

import android.content.Context
import android.graphics.Canvas
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Html
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView

class JustifiedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var justifiedLayout: StaticLayout? = null

    fun setJustifiedText(htmlString: String) {
        val spannedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(htmlString)
        }

        movementMethod = LinkMovementMethod.getInstance()
        linksClickable = true
        text = spannedText

        // Force redraw
        postInvalidate()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onDraw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Native justification for Android 8+
            justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
            super.onDraw(canvas)
            return
        }

        // Manual justification for older devices
        val textPaint: TextPaint = paint
        val textWidth = width - paddingLeft - paddingRight
        val staticLayout = StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
            .build()

        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        staticLayout.draw(canvas)
        canvas.restore()
    }
}