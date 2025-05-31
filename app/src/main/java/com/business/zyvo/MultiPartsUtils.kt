package com.business.zyvo

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object MultiPartsUtils {

    fun uriToMultipartBodyPart(context: Context, uri: Uri, paramName: String): MultipartBody.Part? {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                val tempFile = createTempFile(context, uri)
                copyInputStreamToFile(it, tempFile)
                val requestFile: RequestBody = RequestBody.create(getMimeType(context, uri)?.let { it1 ->
                    it1
                        .toMediaTypeOrNull()
                }, tempFile)
                return MultipartBody.Part.createFormData(paramName, getFileName(context, uri), requestFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun createTempFile(context: Context, uri: Uri): File {
        val tempDir = context.cacheDir
        return File.createTempFile("temp", getFileExtension(context, uri), tempDir)
    }

    private fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    private fun getFileName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        }
        return "file"
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        val mimeType = getMimeType(context, uri)
        return if (mimeType != null) {
            when {
                mimeType.startsWith("image/") -> ".png"
                mimeType.startsWith("video/") -> ".mp4"
                else -> ""
            }
        } else {
            ""
        }
    }
}