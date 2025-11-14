package com.business.zyvo.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.io.ByteArrayOutputStream

open class MediaUtils {

    companion object {

        fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }

        fun setImageFromByteArray(imageBytes: ByteArray, imageView: ImageView) {
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            imageView.setImageBitmap(bitmap)
        }


    }
}