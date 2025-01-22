package com.business.zyvo.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

object ErrorDialog {

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
}