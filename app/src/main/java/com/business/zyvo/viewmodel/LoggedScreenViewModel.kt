package com.business.zyvo.viewmodel

import android.app.Dialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.LogModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoggedScreenViewModel @Inject constructor(private val repository: ZyvoRepository):
    ViewModel() {
    // MutableLiveData to store the list of images
    private val _imageList = MutableLiveData<MutableList<LogModel>>()
    val imageList: LiveData<MutableList<LogModel>> get() = _imageList
    var phoneSignUpLiveData :MutableLiveData<NetworkResult<Pair<String,String>>> = MutableLiveData<NetworkResult<Pair<String,String>>>()
    init {
        // Initialize the list in ViewModel
        loadImages()
    }

    // Function to add images to the list
    private fun loadImages() {
        val images = mutableListOf<LogModel>(
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h")

        )
        _imageList.value = images
    }

     fun signupPhoneNumber(code:String,number :String){
           viewModelScope.launch {
              val result = repository.signUpPhoneNumber(code, number)
              phoneSignUpLiveData.value = result
           }
    }



}