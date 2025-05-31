package com.business.zyvo.viewmodel.host

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.R
import com.business.zyvo.model.PaymentCardModel
import com.business.zyvo.model.TransactionModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PaymentViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel() {

    private var _list = MutableLiveData<ArrayList<TransactionModel>>()
    val list get() = _list
    private var _paymentCardList =MutableLiveData<ArrayList<PaymentCardModel>>()
    val paymentCardList get() = _paymentCardList

    init {
        load()
    }

    private fun load() {
        val transactions = ArrayList<TransactionModel>().apply {
            add(TransactionModel("$65.00", "Pending", R.drawable.ic_mia_pic,
                "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Completed",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Canceled",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Pending", R.drawable.ic_mia_pic,
                "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Completed",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Canceled",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Pending", R.drawable.ic_mia_pic,
                "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Completed",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Canceled",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Pending", R.drawable.ic_mia_pic,
                "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Completed",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Canceled",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Pending", R.drawable.ic_mia_pic,
                "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Completed",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
            add(TransactionModel("$65.00", "Canceled",R.drawable.ic_mia_pic, "Person Name", "May 10, 2023"))
        }

        _list.value = transactions

//        val cards = ArrayList<PaymentCardModel>().apply {
//            add(PaymentCardModel("Indusland","1243","1243","James Bond"))
//            add(PaymentCardModel("Indian Bank","1243","1243","James Bond"))
//            add(PaymentCardModel("Indusland","1243","1243","James Bond"))
//            add(PaymentCardModel("Indusland","1243","1243","James Bond"))
//            add(PaymentCardModel("Indusland","1243","1243","James Bond"))
//            add(PaymentCardModel("Indusland","1243","1243","James Bond"))
//        }

      //  _paymentCardList.value = cards

    }



}