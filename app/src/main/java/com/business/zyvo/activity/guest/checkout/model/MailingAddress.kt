package com.business.zyvo.activity.guest.checkout.model

data class MailingAddress(var user_id: Int = 0,
                           var street_address: String? = "",
                           var city: String? = "",
                           var state: String? = "",
                           var zip_code: String? = "")
