package com.business.zyvo

import android.icu.util.Calendar
import com.business.zyvo.model.CountryLanguage

class AppConstant {

    companion object {
        val VALID_PHONE: String = "Please enter a valid phone."
        val VALID_EMAIL: String = "Please enter a valid email ID."
        val VALID_PASSWORD: String =
            "Password should be minimum eight characters long and should" + "at least have one numeric, one alphabetic and one special character."
        val WISH: String? = "WISH"
        val NOTIFICATION: String = "Notification"
        val FRIEND_ID: String? = "FRIENDiD"
        val CHANNEL_NAME: String = "Channel_Name"
        val HOST_ID: String? = "HOST_ID"
        val CHAT_TOKEN: String? = "chat_token"
        val PENDING: String = "pending"
        val WAITING_PAYMENT1 = "Awaiting payment"
        val CONFIRMED = "confirmed"
        val CANCEL = "cancelled"
        val FINISHED = "finished"
        val BOOKING_ID: String? = "Booking_id"
        val EXTENSION_ID: String? = "Extension_id"
        val PROPERTY_ID: String? = "property_id"
        val DISCOUNT: String = "Discount"
        val BULK_HOUR: String = "Bulk_Hour"
        val MINIMUM_HOUR: String = "Minimum Hour"
        val WHERE: String = "where"
        val TIME: String = "time"
        val ACTIVITY: String = "activity"
        const val textType: String = "textType"
        var LOGIN_SESSION = "Loggin_setion"
        var Host = "host"
        var Guest = "guest"
        var PANNEL = "Pannel"
        var AuthToken = "AuthToken"
        var Name1 = "FullName"
        val USER_VERIFIED: String? = "user_verified"
        val USER_IMAGE: String? = "user_image"
        val Button = "button"
        val USER_ID: String? = "USER_ID"
        val PRICE: String = "PRICE"
        val type:String? = "type"
        val profileType:String = "street"
        val article :String= "article"
        val Id: String?="ID"
        val NEEDMORE: String?="needmore"
        val session: String?="UserSession"
        val LATITUDE:String ="latitude"
        val LATITUDEGUST:String ="latitudegust"
        val LONGITUDE :String ="longitude"
        val LONGITUDEGUST :String ="longitudegust"
        val NAME: String? = "NAME"
        val MOVE: String = "MOVE"
        val DELETE: String = "delete"
        val ARCHIVED: String = "Archived"
        val Image: String = "image"
        val REPORT: String = "report"
        val BLOCK: String = "block"
        val MUTE: String = "mute"
        val ADD_IMAGE: String = "ADD_IMAGE"
        val EDIT: String = "edit"
        val USER: String = "User"
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1900..currentYear).toList()
        val FILTERREQUEST: String = "filterRequest"
        val SEARCHFILTERREQUEST: String = "searchfilterRequest"
        val months = listOf(
            "January", "February", "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December"
        )
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

        //All Error Message
        val email = "Email address is required"
        val invalideemail = "Invalid Email Address"
        val password = "Password is required"
        val conPassword = "Confirm Password is required"


        val mobile = "Phone number is required"

        val locationCheck = "Please Add Location Details"
        val languageCheck = "Please Add Language Details"
        val name = "Name is required"
        val firstName = "First name is required"
        val lastName = "Last name is required"
        val otp = "Please enter the OTP"
        val unKnownError = "There was an unknown error. Check your connection, and try again."
        val unknownLocation = "Unknown Location"
        val aboutMe = "About Me is required"
        val details = "Details is required"
        val feedbackAbout = "Please select feedback about"
        val hours = "Hours is required"
        val price = "Price is required"
        val stTime = "Start time is required"
        val edTime = "End time is required"
        val message = "Message is required"
        val emailValid = "Please enter a valid email"
        val description = "Description is required"
        val avabilty = "Please choose an availability window within 24 hours."
        val placeOpenActivity = "placeOpenActivity"
        val property_images = "property_images"
        val latitude = "latitude"
        val longitude = "longitude"
        val property_rating = "property_rating"
        val property_status = "property_status"
        val property_review_count = "property_review_count"
        val distance_miles = "distance_miles"
        val title = "title"


        val cardName = "Card holder name is required"

        val cardMonth = "Card month is required"

        val cardYear = "Card year is required"

        val cardCVV = "Card Cvv is required"
        val selectCard = "Please select Card"

        val additional = "Additional Details is required"
        val spinner = "Please select a reason"

        const val firstNameError: String = "First name can't be empty."
        const val lastNameError: String = "Last name can't be empty."
        const val emailError = "Email can't be empty."
        const val phoneError = "phone can't be empty."
        const val dobError: String = "Please select DOB"
        const val selectIdTypeError: String = "Select id type"
        const val pINError: String = "Personal identification number can't be empty."
        const val SNNError: String = "SSN can't be empty."
        const val SNNValidError: String = "Enter Valid SSN."
        const val addressError: String = "Address can't be empty."
        const val countryError: String = "Select country."
        const val stateError: String = "Select State."
        const val cityError: String = "Select City."
        const val postalCodeError: String = "Postal code can't be empty."
        const val bankNameError: String = "Bank Name can't be empty."
        const val cardholderError = "Card Holder Name can't be empty."
        const val accountNumberError = "Account number can't be empty."
        const val cAccountNumberError = "Confirm account number can't be empty."
        const val routingNumberError = "Routing number can't be empty."
        const val proofofbanError = "Please upload proof of bank account."
        const val frontimageError = "Please upload front image."
        const val backimageError = "Please upload back image."
        const val validEmail: String = "Please Enter Valid Email Address"
        const val validPhoneNumber: String = "Please Enter Valid Phone Number"
        //const val Image : String = "image"
        const val propertyList: String = "propertyList"
       const val  loginType : String  = "LoginType"
        const val passwordMustConsist =
            "The password must consist of at least 8 characters and include at least 1 number, 1 uppercase letter, and 1 special character."

        const val validAvailability: String = "Please enter minimum set hours"

    }


}