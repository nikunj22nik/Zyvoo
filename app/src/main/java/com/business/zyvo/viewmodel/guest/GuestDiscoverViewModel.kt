package com.business.zyvo.viewmodel.guest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.model.LogModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GuestDiscoverViewModel @Inject constructor(private val repository: ZyvoRepository):
    ViewModel(){

    private val _imageList = MutableLiveData<MutableList<LogModel>>()
    val imageList: LiveData<MutableList<LogModel>> get() = _imageList

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



}