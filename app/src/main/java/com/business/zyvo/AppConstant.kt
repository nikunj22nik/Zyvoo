package com.business.zyvo

import android.icu.util.Calendar
import com.business.zyvo.model.CountryLanguage

class AppConstant {

    companion object {
        val WHERE: String = "where"
        val TIME :String = "time"
        val ACTIVITY :String = "activity"
        const val textType : String = "textType"
        var LOGIN_SESSION = "Loggin_setion"
        var Host = "Host"
        var Guest = "Guest"
        val DATE: String? = "event_date"
        val MY_INTERESET: String? ="my_interest"
        val START_TIME: String? = "START_TIME"
        val END_TIME :String? = "end_time"
        val Match_NUMBER: String? = "match Number"
        val UNIQUE_NUMBER: String? = "Unique Number"
        val CATEGORY_NAME:String? = "Category_name"
        val USER_VERIFIED: String? = "user_verified"
        val CATEGORY_ID: String? = "Category_id"
        val BUSINESS_ID: String? = "BUSINESS_ID"
        val CUSTOMER_ID: String? = "customer_id"
        val EVENT_QUANTITY: String? = "EVENT_QUANTITY"
        val VOOPON_QUANTITY: String? = "VOOPON_QUANTITY"
        val BUSINESS_VOOPON_ID: String? = "BUSINESS_VOOPON_ID"
        val PROMOTER_VOOPON_ID: String? ="PROMOTER_VOOPON_ID"
        val BUSINESS_EVENT_ID: String? ="BUSINESS_EVENT_ID"
        val PROMOTER_EVENT_ID: String? = "PROMOTER_EVENT_ID"
        val Button= "button"
        val USERTYPE: String? ="USER_TYPE"
        val BUSSINESS_ID: String? ="business_id"
        val EVENT_NAME: String? ="event_name"
        val MOVING_HOME_TO_LOCATION_MAP: Int =321
        val EDIT_EVENT: String = "edit_event"
        val EDIT_VOOPON :String = "edit_voopon"
        val PROFILE_IMAGE: String?="profile_image"
        val LOCATION_PERMISSION: Int=150
        val REFERRAL_CODE: String? ="referal_code"
        val PROMOTER_ID: String? ="PROMOTER_ID"
        val USER_ID: String? ="USER_ID"
        val VOOPON_IMAGE: String? ="voopon_image"
        val END_DATE: String? ="END_DATE"
        val START_DATE: String? ="START_DATE"
        val DESCRIPTION: String? = "DESCRIPTION"
        val VOOPON_NAME: String? = "VOOPON_NAME"
        val SENDING_SCANNED_DATA: String? ="SCANNED_DATA"
        val BankModel: String? = "Bank Model"
        val ADDRESS_TYPE: String ="Address_Type"
        val LOCATION: String? ="LOCATION"
        val PRICE: String?="PRICE"
        val PLAN_ID:String ="plan_id   "
        val Network_dialouge: String ="DIALOG_NETWORK"
        val HOME: String = "Home"
        val OFFICE :String = "Office"
        val HOTEL:String = "Hotel"
        val OTHERS:String = "Others"
        val EVENT_ID: String?="EVENT_ID"
        val PROVIDER_ID: String?="PROVIDER_ID"
        val type:String? = "type"
        val article :String= "article"
        val VOOPON_ID :String? = "VOOPOM_ID"
        val Id: String?="ID"
        val LATITUDE:String ="latitude"
        val ADDRESS_ID :String ="Address_id"
        val ADDRESS:String = "Address"
        val LONGITUDE :String ="longitude"
        val OTP: String? ="OTP"
        val DEVICE_TYPE: String? = "Delete"
        val FCM_TOKEN: String? = "fcm_token"
        val NAME: String? ="NAME"
        val ACCOUNT_NUMBER :String?= "AccountNumber"
        val ROUTING_NUMBER :String? = "RoutingNumber"
        val BankResultCode :Int =30
        val EMAIL :String ="Email"
        val PASSWORD :String ="password"
        val UPCOMING: String?="upcoming"
        val COMPLETED :String? = "complete"
        val SHARE_VOOPON: String="SHARE_VOOPON"
        val MOVE: String="MOVE"
        val DELETE: String="delete"
        val ADD_IMAGE :String = "ADD_IMAGE"
        val EDIT:String="edit"
        val CREATE_VOOPON: String="CREATE_VOOPON"
        val CREATE_EVENT: String="CREATE_EVENT"
        val USER: String="User"
        val BUSSINESS="Business"
        val PROMOTER="Promoter"
        val FROMBUSSINESS="fromBussiness"
        var TITLE="title"
        val EVENT = "event"
        val VOOPON ="Voopon"
        val BASE_URL ="https://vooleyvoo.tgastaging.com"
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1900..currentYear).toList()

        val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December")

        val countriesAndLanguages = listOf(
            CountryLanguage("United States", "English"),
            CountryLanguage("Canada", "English"),
            CountryLanguage("Mexico", "Spanish"),
            CountryLanguage("Brazil", "Portuguese"),
            CountryLanguage("Argentina", "Spanish"),
            CountryLanguage("United Kingdom", "English"),
            CountryLanguage("Germany", "German"),
            CountryLanguage("France", "French"),
            CountryLanguage("Italy", "Italian"),
            CountryLanguage("Spain", "Spanish"),
            CountryLanguage("India", "Hindi"),
            CountryLanguage("China", "Mandarin"),
            CountryLanguage("Japan", "Japanese"),
            CountryLanguage("South Korea", "Korean"),
            CountryLanguage("Russia", "Russian"),
            CountryLanguage("Australia", "English"),
            CountryLanguage("Saudi Arabia", "Arabic"),
            CountryLanguage("Egypt", "Arabic"),
            CountryLanguage("South Africa", "Afrikaans"),
            CountryLanguage("Nigeria", "English"),
            CountryLanguage("Kenya", "Swahili"),
            CountryLanguage("Turkey", "Turkish"),
            CountryLanguage("Iran", "Persian"),
            CountryLanguage("Thailand", "Thai"),
            CountryLanguage("Vietnam", "Vietnamese"),
            CountryLanguage("Pakistan", "Urdu"),
            CountryLanguage("Indonesia", "Indonesian"),
            CountryLanguage("Poland", "Polish"),
            CountryLanguage("Ukraine", "Ukrainian"),
            CountryLanguage("Israel", "Hebrew"),
            CountryLanguage("Chile", "Spanish"),
            CountryLanguage("Colombia", "Spanish"),
            CountryLanguage("Peru", "Spanish"),
            CountryLanguage("Sweden", "Swedish"),
            CountryLanguage("Norway", "Norwegian"),
            CountryLanguage("Denmark", "Danish"),
            CountryLanguage("Finland", "Finnish"),
            CountryLanguage("Netherlands", "Dutch"),
            CountryLanguage("Belgium", "Dutch")
        )
    }


}