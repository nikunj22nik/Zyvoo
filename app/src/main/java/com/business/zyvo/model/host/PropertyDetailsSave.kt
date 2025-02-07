package com.business.zyvo.model.host

class PropertyDetailsSave {
    var user_id =1;
    var property_id = -1
    var space_type =""
    var property_size :Int= -1
    var max_guest_count :Int = -1
    var bedroom_count :Int = -1
    var bathroom_count :Int= -1
    var is_instant_book : Boolean = false
    var has_self_checkin : Boolean = false
    var allows_pets : Boolean = false
    var cancellation_duration :  Int =24
    var title :String =""
    var description : String =""
    var parking_rules: String =""
    var host_rules:String =""
    var street_address : String =""
    var city: String =""
    var zip_code: String =""
    var country: String =""
    var state : String =""
    var latitude : Float =0.000f
    var longitude : Float = 0.0000f
    var min_booking_hours : Int=0
    var hourly_rate:Int =0
    var bulk_discount_hour : Int =0
    var bulk_discount_rate : Int =0
    var cleaning_fee: Float =0.00f
    var available_month ="00"
    var available_day ="all"
    var available_from="00:00"
    var available_to="00:00"
    var images : MutableList<String> = mutableListOf()
    var activities :MutableList<String> = mutableListOf()
    var amenities : MutableList<String> = mutableListOf()
    var add_ons : MutableList<AddOnModel> = mutableListOf()
     var fname:String?= null
    var lname :String? = null

}