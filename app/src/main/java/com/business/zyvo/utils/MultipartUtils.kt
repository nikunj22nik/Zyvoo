package com.business.zyvo.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object MultipartUtils {


    fun isPhoneNumberMatchingCountryCode(userInputNumber: String, countryPhoneCode: String): Boolean {

        val phoneUtil = PhoneNumberUtil.getInstance()

        // Normalize country phone code (e.g., "+91" or "91" → "91")
        val normalizedCountryCode = countryPhoneCode.replace("+", "").trim()

        // Clean user input: keep only digits and optional leading +
        val cleanNumber = userInputNumber.replace("[^\\d+]".toRegex(), "")

        // Construct the full international number
        val internationalNumber = when {
            cleanNumber.startsWith("+") -> cleanNumber
            cleanNumber.startsWith(normalizedCountryCode) -> "+$cleanNumber"
            else -> "+$normalizedCountryCode$cleanNumber"
        }

        return try {

            // Get ISO region from country phone code (e.g., "91" → "IN")
            val regionCode = phoneUtil.getRegionCodeForCountryCode(normalizedCountryCode.toInt())
            // If region code is invalid, return false

            if (regionCode.isNullOrEmpty()) {
                Log.e("PhoneValidation", "Invalid country code: $normalizedCountryCode")
                return false
            }
            // Parse the phone number with the region

            val numberProto = phoneUtil.parse(internationalNumber, regionCode)

            // Validate actual country code

            val actualCountryCode = numberProto.countryCode.toString()
            val isValid = phoneUtil.isValidNumber(numberProto)

            val isMatch = actualCountryCode == normalizedCountryCode && isValid

            Log.d(
                "PhoneValidation",
                "Input: $userInputNumber → Normalized: $internationalNumber | " +
                        "Expected Code: $normalizedCountryCode | Actual Code: $actualCountryCode | " +
                        "Valid: $isValid | Match: $isMatch"
            )

            isMatch
        } catch (e: NumberParseException) {
            Log.e("PhoneValidation", "Invalid phone number: ${e.message}")
            false
        }
    }

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