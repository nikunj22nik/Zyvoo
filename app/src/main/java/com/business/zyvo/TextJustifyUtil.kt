package com.business.zyvo
import android.widget.TextView

class TextJustifyUtil {

    companion object {
        fun TextView.justifyText() {
            val original = this.text.toString()
            val words = original.split(" ")
            if (words.size <= 1) return

            this.post {
                val textViewWidth = this.width - this.paddingLeft - this.paddingRight
                val paint = this.paint

                val builder = StringBuilder()
                var lineWords = mutableListOf<String>()
                var lineWidth = 0f

                for (word in words) {
                    val testLine = (lineWords + word).joinToString(" ")
                    val testWidth = paint.measureText(testLine)

                    if (testWidth < textViewWidth) {
                        lineWords.add(word)
                        lineWidth = testWidth
                    } else {
                        // justify this line
                        if (lineWords.size > 1) {
                            val spacesToAdd = textViewWidth - paint.measureText(lineWords.joinToString(" "))
                            val gaps = lineWords.size - 1
                            val extraSpace = spacesToAdd / gaps

                            val justifiedLine = buildString {
                                for (i in lineWords.indices) {
                                    append(lineWords[i])
                                    if (i < gaps) {
                                        append(" ")
                                        append(" ".repeat((extraSpace / paint.measureText(" ")).toInt()))
                                    }
                                }
                            }
                            builder.append(justifiedLine).append("\n")
                        } else {
                            builder.append(lineWords.joinToString(" ")).append("\n")
                        }

                        lineWords = mutableListOf(word)
                    }
                }

                // Last line (normal, no justify)
                builder.append(lineWords.joinToString(" "))

                this.text = builder.toString()
            }
        }

    }
}