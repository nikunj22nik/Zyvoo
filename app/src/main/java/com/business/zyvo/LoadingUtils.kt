package com.business.zyvo

import android.content.Context

open class LoadingUtils {

    companion object {
        private var jarvisLoader: JarvisLoader? = null
        fun showDialog(context: Context?,
                       isCancelable: Boolean
        ) {
            hideDialog()
            if (context != null) {
                try {
                    jarvisLoader = JarvisLoader(context)
                    jarvisLoader?.let { jarvisLoader->
                        jarvisLoader.setCanceledOnTouchOutside(true)
                        jarvisLoader.setCancelable(isCancelable)
                        jarvisLoader.show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun hideDialog() {
            if (jarvisLoader!=null && jarvisLoader?.isShowing!!) {
                jarvisLoader = try {
                    jarvisLoader?.dismiss()
                    null
                } catch (e: Exception) {
                    null
                }
            }
        }

    }
}