package com.business.zyvo.model

data class PaymentCardModel(
    val id: String?,
    val bankName: String? = null,
    val cardHolderName: String? = null,
    val accountHolderName: String? = null,
    val cardFirstNumber: String? = null,
    val cardEndNumber: String? = null,
    val isBankAccount: Boolean  ,
    val defaultForCurrency: Boolean


)
