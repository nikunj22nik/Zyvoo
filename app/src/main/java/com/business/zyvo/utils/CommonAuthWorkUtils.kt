package com.business.zyvo.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.business.zyvo.R
import com.business.zyvo.activity.AuthActivity
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.session.SessionManager
import com.business.zyvo.viewmodel.LoggedScreenViewModel
import com.hbb20.CountryCodePicker


class CommonAuthWorkUtils(var context: Context, navController: NavController?) {

    var navController = navController
    private lateinit var otpDigits: Array<EditText>
    private var countDownTimer: CountDownTimer? = null
    var resendEnabled = false
    var otpValue: String = ""



    private fun showPopupWindow(anchorView: View, position: Int)   {
        // Inflate the custom layout for the popup menu
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_filter_all_conversations, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemAllConversations).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemArchived).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemUnread).setOnClickListener {

            popupWindow.dismiss()
        }


        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = context?.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow!!) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }

        // Show the popup window anchored to the view (three-dot icon)
        popupWindow.elevation = 8.0f  // Optional: Add elevation for shadow effect
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)  // Adjust the Y offset dynamically
    }

     fun isScreenLarge(context: Context): Boolean {
        // Get the screen width
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val width = display.width

        // Convert pixels to dp
        val density = context.resources.displayMetrics.density
        val widthInDp = (width / density).toInt()

        // Check if the screen width is greater than 600dp (typical for tablets)
        return widthInDp > 600
    }

}