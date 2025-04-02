package com.business.zyvo.session

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import com.business.zyvo.AppConstant
import com.business.zyvo.MyApp
import com.business.zyvo.model.AddLanguageModel
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


    fun setNotificationOnOffStatus(value :Boolean){

        editor!!.putBoolean(AppConstant.NOTIFICATION, value)
        editor!!.commit()

    }

    fun getNotificationStatus() : Boolean {
        return pref?.getBoolean(AppConstant.NOTIFICATION,false)?:false
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

    fun setNeedMore(id:Boolean){
        editor!!.putBoolean(AppConstant.NEEDMORE,id)
        editor!!.commit()
    }

    fun getNeedMore():Boolean?{
        return pref?.getBoolean(AppConstant.NEEDMORE,false)
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

    fun setName(name:String){
        editor!!.putString(AppConstant.Name1,name)
        editor!!.commit()
    }

    fun getName():String?{
        return pref?.getString(AppConstant.Name1,"")
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
    // Remove a language from the stored list
    fun removeLanguage(context: Context, languageName: String) {
        val languages = getLanguages(context).toMutableList()

        // Remove the language by name
        val languageToRemove = languages.find { it.name == languageName }
        if (languageToRemove != null) {
            languages.remove(languageToRemove)
            // Save the updated list back to SharedPreferences
            saveLanguages(context, languages)
        }
    }


    private  val LANGUAGES_KEY = "languages"

    // Save a list of AddLanguageModel to SharedPreferences
    fun saveLanguages(context: Context, languages: List<AddLanguageModel>) {


        // Convert the list to a JSON string
        val gson = Gson()
        val json = gson.toJson(languages)

        // Save the JSON string to SharedPreferences
        editor?.putString(LANGUAGES_KEY, json)
        editor?.apply()
    }

    // Fetch the list of AddLanguageModel from SharedPreferences
    fun getLanguages(context: Context): List<AddLanguageModel> {


        // Get the stored JSON string
        val json = pref?.getString(LANGUAGES_KEY, null)

        // If no data, return an empty list
        if (json == null) {
            return emptyList()
        }

        // Deserialize the JSON string into a list of AddLanguageModel objects
        val gson = Gson()
        val type = object : TypeToken<List<AddLanguageModel>>() {}.type
        return gson.fromJson(json, type)
    }

    // Check if a language is already stored
    fun isLanguageStored(context: Context, languageName: String): Boolean {
        val languages = getLanguages(context)
        return languages.any { it.name == languageName }
    }




}