package com.business.zyvo.fragment.both.completeProfile.model

class CompleteProfileReq {
    var user_id =1;
    var first_name =""
    var last_name :String =""
    var about_me : String =""
    var street_address: String =""
    var city:String =""
    var state : String =""
    var zip_code: String =""
    var bytes: ByteArray = byteArrayOf()
    var where_live : MutableList<String> = mutableListOf()
    var works :MutableList<String> = mutableListOf()
    var languages : MutableList<String> = mutableListOf()
    var hobbies : MutableList<String> = mutableListOf()
    var pets : MutableList<String> = mutableListOf()
    var identityVerified = 0

}