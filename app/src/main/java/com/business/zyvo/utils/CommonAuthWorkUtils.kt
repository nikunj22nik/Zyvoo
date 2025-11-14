package com.business.zyvo.utils

import android.content.Context
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.EditText
import androidx.navigation.NavController


class CommonAuthWorkUtils(var context: Context, navController: NavController?) {

    var navController = navController
    var resendEnabled = false

     fun isScreenLarge(context: Context): Boolean {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val width = display.width

        // Convert pixels to dp
        val density = context.resources.displayMetrics.density
        val widthInDp = (width / density).toInt()
        return widthInDp > 600
    }

}