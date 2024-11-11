package com.yesitlab.zyvo

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class ObservableTextView  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    // Define a listener interface
    private var onTextChangedListener: OnTextChangedListener? = null

    // Set the listener for observing text changes
    fun setOnTextChangedListener(listener: OnTextChangedListener) {
        onTextChangedListener = listener
    }

    // Override the setText method to observe text changes
    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        onTextChangedListener?.onTextChanged(text.toString())
    }

    // Define the interface for text change notification
    interface OnTextChangedListener {
        fun onTextChanged(newText: String)
    }
}