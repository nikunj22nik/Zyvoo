package com.business.zyvo.fragment.guest.bookingviewmodel.dataclass

data class Charges(
    val addon: Int? = 0,
    val cleaning_fee: String? = "0",
    val service_fee: String? = "0",
    val taxes: String? = "0",
    val time_charge: String? = "0",
    val total: String? = "0"
)
