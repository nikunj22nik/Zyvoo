package com.business.zyvo.fragment.host.payments.model

import com.google.gson.annotations.SerializedName

data class GetPayoutResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: PayoutData?
)

data class PayoutData(
    @SerializedName("bank_accounts") val bankAccounts: MutableList<BankAccountPayout>?,
    @SerializedName("cards") val cards: MutableList<CardPayout>?
)

data class BankAccountPayout(
    @SerializedName("id") val id: String?,
    @SerializedName("bank_name") val bankName: String?,
    @SerializedName("last_four_digits") val lastFourDigits: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("default_for_currency") val defaultForCurrency: Boolean,
    @SerializedName("account_holder_name") val accountHolderName: String?,
    @SerializedName("account_holder_type") val accountHolderType: String?,
    @SerializedName("status") val status: String?
)

data class CardPayout(
    @SerializedName("id") val id: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("last_four_digits") val lastFourDigits: String?,
    @SerializedName("exp_month") val expMonth: Int,
    @SerializedName("exp_year") val expYear: Int,
    @SerializedName("default_for_currency") val defaultForCurrency: Boolean,
    @SerializedName("currency") val currency: String?,
    @SerializedName("card_holder_name") val cardHolderName: String?
)


