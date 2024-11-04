package com.yesitlab.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yesitlab.zyvo.model.AddPaymentCardModel
import com.yesitlab.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel  @Inject constructor(private val repository: ZyvoRepository) : ViewModel(){

    private val _paymentCardList = MutableLiveData<MutableList<AddPaymentCardModel>>()
    val paymentCardList : LiveData<MutableList<AddPaymentCardModel>> get() =  _paymentCardList


    init {
        loadPaymentDetail()
    }


    private fun loadPaymentDetail(){
        val paymentList = mutableListOf<AddPaymentCardModel>(
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888")
        )

        _paymentCardList.value = paymentList

    }

}