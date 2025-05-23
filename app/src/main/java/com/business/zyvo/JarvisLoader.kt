package com.business.zyvo

import android.app.Dialog
import android.content.Context
import android.os.Bundle

import android.widget.LinearLayout



class JarvisLoader(context: Context) : Dialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawableResource(com.google.android.gms.wallet.R.color.wallet_dim_foreground_disabled_holo_dark)
    }


}