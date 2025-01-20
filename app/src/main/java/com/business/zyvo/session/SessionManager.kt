package com.business.zyvo.session

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import com.business.zyvo.AppConstant

class SessionManager(var context: Context) {

    var dialog: Dialog?= null
    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor?=null

    init{
        pref=context.getSharedPreferences(AppConstant.LOGIN_SESSION, Context.MODE_PRIVATE)
        editor=pref?.edit()
    }

    fun setUserType(userType: String) {
        editor!!.putString(AppConstant.USER, userType)
        editor!!.commit()
    }


    fun getUserType():String?{
        return pref?.getString(AppConstant.USER,"")
    }

    fun setUserVerified(verified:Boolean){
        editor!!.putBoolean(AppConstant.USER_VERIFIED, verified)
        editor!!.commit()
    }

    fun getUserVerified() :Boolean{
        return pref?.getBoolean(AppConstant.USER_VERIFIED,false) ?: false
    }


    fun setUserId(id:Int){
        editor!!.putInt(AppConstant.Id,id)
        editor!!.commit()
    }

    fun getUserId():Int?{
        return pref?.getInt(AppConstant.Id,-1)
    }


}