package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.model.HostListingModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HostListingViewModel @Inject constructor(private val repository: ZyvoRepository):
    ViewModel(){

    private val _imageList = MutableLiveData<MutableList<HostListingModel>>()
    val imageList: LiveData<MutableList<HostListingModel>> get() = _imageList

    init {
        loadImages()
    }

    private fun loadImages() {
        val images = mutableListOf<HostListingModel>(
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h"),
            HostListingModel("Cabin in Peshastin","4.0", "(1k+)", "37 miles away", "\$12 / h")

        )
        _imageList.value = images
    }

}