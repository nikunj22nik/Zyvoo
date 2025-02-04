package com.business.zyvo.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.business.zyvo.R
import com.business.zyvo.databinding.CustomDialogBinding
import java.util.Timer
import java.util.TimerTask

object ErrorDialog {

    const val TAG = "*****ZYVO"
    fun showErrorDialog(activity: Context, message: String) {
        // Create and build the AlertDialog
        val dialog = AlertDialog.Builder(activity)
            .setTitle("Error") // Title of the dialog
            .setMessage(message) // Error message to show
            .setCancelable(false) // Set cancelable to false to prevent dismissing by tapping outside
            .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss() // Dismiss the dialog when 'OK' is pressed
            }
            .create()

        // Show the dialog
        dialog.show()
    }

    fun customDialog(string: String?, context: Context? = null) {
        try {
            string?.let {
                val dialogBinding: CustomDialogBinding? =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.custom_dialog,
                        null,
                        false
                    )
                val customDialog = AlertDialog.Builder(context!!, 0).create()
                customDialog.apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    setView(dialogBinding?.root)
                    setCancelable(false)
                }.show()
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        try {
                            if (customDialog.isShowing) {
                                customDialog.dismissSafe()
                            }
                            timer.cancel()
                        } catch (e: Exception) { }
                    }
                }, 3500)
                dialogBinding?.textViewcustomdialog?.text = string
                dialogBinding?.imageView29?.setSafeOnClickListener {
                    if (customDialog.isShowing) {
                        customDialog.dismissSafe()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("dialog","dialog exception ${e.message}")
        }
    }
    fun AlertDialog.dismissSafe() {
        window?.let {
            if (it.decorView != null && it.decorView.parent != null) {
                this.dismiss()
            }
        } ?: run {
            return
        }
    }

    class SafeClickListener(private var defaultInterval: Int = 1000,
                            private val onSafeCLick: (View) -> Unit) : View.OnClickListener {

      private var lastTimeClicked: Long = 0

      override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }

    }

    private fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }



}