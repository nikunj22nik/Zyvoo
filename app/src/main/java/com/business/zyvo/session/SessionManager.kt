package com.business.zyvo.session

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import com.business.zyvo.AppConstant
import com.business.zyvo.MyApp
import com.business.zyvo.model.ChannelListModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

    fun setChatToken(token :String){
        editor!!.putString(AppConstant.CHAT_TOKEN, token)
        editor!!.commit()

        val baseApp = context.applicationContext as MyApp

    }

    fun getChatToken() : String? {
        return pref?.getString(AppConstant.CHAT_TOKEN,"")
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

    fun setUserSession(session:Boolean){
        editor!!.putBoolean(AppConstant.session,session)
        editor!!.commit()
    }

    fun getUserSession():Boolean?{
        return pref?.getBoolean(AppConstant.session,false)
    }

    fun setAuthToken(token:String){
        editor!!.putString(AppConstant.AuthToken,token)
        editor!!.commit()
    }

    fun getAuthToken():String?{
        return pref?.getString(AppConstant.AuthToken,"")
    }
    fun logOut() {
        editor?.clear()
        editor?.apply()
    }


    fun getCurrentPanel() : String {
        return pref?.getString(AppConstant.PANNEL,"")?:""
    }
    fun setCurrentPanel(pannel :String){
        editor!!.putString(AppConstant.PANNEL,pannel)
         editor!!.commit()
    }

    fun setLatitude(latitude :String){
        editor!!.putString(AppConstant.LATITUDE,latitude)
        editor!!.commit()
    }

    fun setLongitude(longitude :String){
        editor!!.putString(AppConstant.LONGITUDE,longitude)
        editor!!.commit()
    }


    fun setGustLatitude(latitude :String){
        editor!!.putString(AppConstant.LATITUDEGUST,latitude)
        editor!!.commit()
    }

    fun setGustLongitude(longitude :String){
        editor!!.putString(AppConstant.LONGITUDEGUST,longitude)
        editor!!.commit()
    }


    fun getLatitude(): String{
        return pref?.getString(AppConstant.LATITUDE,"")?:""
    }

    fun getLongitude() : String{
        return pref?.getString(AppConstant.LONGITUDE,"")?:""
    }

    fun getGustLatitude(): String{
        return pref?.getString(AppConstant.LATITUDEGUST,"")?:""
    }

    fun getGustLongitude() : String{
        return pref?.getString(AppConstant.LONGITUDEGUST,"")?:""
    }



    fun setFilterRequest(filterRequest :String){
        editor!!.putString(AppConstant.FILTERREQUEST,filterRequest)
        editor!!.commit()
    }

    fun getFilterRequest(): String{
        return pref?.getString(AppConstant.FILTERREQUEST,"")?:""
    }

    fun setSearchFilterRequest(filterRequest :String){
        editor!!.putString(AppConstant.SEARCHFILTERREQUEST,filterRequest)
        editor!!.commit()
    }

    fun getSearchFilterRequest(): String{
        return pref?.getString(AppConstant.SEARCHFILTERREQUEST,"")?:""
    }


    fun saveChannelListToPreferences(context: Context, key: String, channelList: MutableList<ChannelListModel>) {
        // Convert MutableList<ChannelListModel> to JSON string
        val gson = Gson()
        val jsonString = gson.toJson(channelList)

        // Save the JSON string to SharedPreferences
        editor?.putString(key, jsonString)
        editor?.apply()
    }

    fun getChannelListFromPreferences(context: Context, key: String): MutableList<ChannelListModel>? {
        val jsonString = pref?.getString(key, null)

        if (jsonString != null) {
            // Convert JSON string back to MutableList<ChannelListModel>
            val gson = Gson()
            val type = object : TypeToken<MutableList<ChannelListModel>>() {}.type
            return gson.fromJson(jsonString, type)
        }

        return null // Return null if no data found
    }




}