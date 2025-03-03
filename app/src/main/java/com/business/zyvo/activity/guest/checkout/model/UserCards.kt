package com.business.zyvo.activity.guest.checkout.model

data class UserCards(  val card_id: String = "",
                       val brand: String = "",
                       val last4: String = "",
                       val exp_month: String = "",
                       val exp_year: String = "",
                       val cardholder_name: String = "",
                       val billing_address: BillingAddress?,
                       val is_preferred: Boolean = false)

data class BillingAddress(
    val city: String? = "",
    val country: String? = "",
    val line1: String? = "",
    val line2: String? = "",
    val postal_code: String? = "",
    val state: String? = ""
)
