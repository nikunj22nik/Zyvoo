package com.business.zyvo.model

data class TransactionModel(
    val amount: String,
    val status: String,
    val image: Int,
    val guestName: String,
    val date: String
)
