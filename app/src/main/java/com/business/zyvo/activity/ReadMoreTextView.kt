package com.business.zyvo.activity.guest

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.business.zyvo.R


class ReadMoreTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var fullText: String = ""
    private var isExpanded = false
    private var maxCollapsedLines = 3
    private var readMoreText = "Read More"
    private var readLessText = "Read Less"
    private var readMoreColor = Color.BLUE
    private var collapsedText: String = ""

    init {
        // Read custom attributes if needed
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ReadMoreTextView,
            0, 0
        ).apply {
            try {
                maxCollapsedLines = getInt(R.styleable.ReadMoreTextView_maxCollapsedLines, 3)
                readMoreText = getString(R.styleable.ReadMoreTextView_readMoreText) ?: "Read More"
                readLessText = getString(R.styleable.ReadMoreTextView_readLessText) ?: "Read Less"
                readMoreColor = getColor(R.styleable.ReadMoreTextView_readMoreColor, Color.BLUE)
            } catch (e: Exception) {
                // attrs.xml not found, use defaults
            } finally {
                recycle()
            }
        }
    }

    fun setFullText(text: String) {
        fullText = text
        isExpanded = false
        setText(text)

        // Post to ensure layout is ready
        post {
            checkAndSetupReadMore()
        }
    }

    private fun checkAndSetupReadMore() {
        if (fullText.isEmpty()) return

        val layout = layout ?: return

        if (layout.lineCount > maxCollapsedLines) {
            calculateCollapsedText()
            showCollapsedText()
        }
    }

    private fun calculateCollapsedText() {
        super.setMaxLines(maxCollapsedLines)

        post {
            val layout = layout ?: return@post

            if (layout.lineCount > 0) {
                val lastLineEnd = layout.getLineEnd(maxCollapsedLines - 1)
                val substringEnd = (lastLineEnd - readMoreText.length - 3).coerceAtLeast(0)
                val truncatedText = fullText.substring(0, substringEnd)
                collapsedText = "$truncatedText... $readMoreText"
            }
        }
    }

    private fun showCollapsedText() {
        isExpanded = false
        super.setMaxLines(maxCollapsedLines)

        // Agar collapsed text already calculated hai to turant use karo
        if (collapsedText.isNotEmpty()) {
            applyClickableSpan(collapsedText, readMoreText) {
                showExpandedText()
            }
        } else {
            // Pehli baar calculate karo
            post {
                val layout = layout ?: return@post

                if (layout.lineCount > 0) {
                    val lastLineEnd = layout.getLineEnd(maxCollapsedLines - 1)
                    val substringEnd = (lastLineEnd - readMoreText.length - 3).coerceAtLeast(0)
                    val truncatedText = fullText.substring(0, substringEnd)
                    collapsedText = "$truncatedText... $readMoreText"

                    applyClickableSpan(collapsedText, readMoreText) {
                        showExpandedText()
                    }
                }
            }
        }
    }

    private fun showExpandedText() {
        isExpanded = true
        super.setMaxLines(Int.MAX_VALUE)
        val displayText = "$fullText $readLessText"

        applyClickableSpan(displayText, readLessText) {
            showCollapsedText()
        }
    }

    private fun applyClickableSpan(displayText: String, clickableText: String, onClick: () -> Unit) {
        val spannableString = SpannableString(displayText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClick()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = readMoreColor
                ds.isUnderlineText = false
                ds.isFakeBoldText = true
            }
        }

        val startIndex = displayText.length - clickableText.length
        if (startIndex >= 0) {
            spannableString.setSpan(
                clickableSpan,
                startIndex,
                displayText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        setText(spannableString, BufferType.SPANNABLE)
        movementMethod = LinkMovementMethod.getInstance()
        highlightColor = Color.TRANSPARENT
    }

    fun setCollapsedLines(lines: Int) {
        maxCollapsedLines = lines
    }

    fun setReadMoreColor(color: Int) {
        readMoreColor = color
    }
}