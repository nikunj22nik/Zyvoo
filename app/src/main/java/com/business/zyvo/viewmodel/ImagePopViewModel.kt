package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.R
import com.business.zyvo.model.ViewpagerModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImagePopViewModel @Inject constructor(private val repository: ZyvoRepository):
    ViewModel(){

    // MutableLiveData to store the list of images
    private val _imageList = MutableLiveData<MutableList<ViewpagerModel>>()
    val imageList: LiveData<MutableList<ViewpagerModel>> get() = _imageList

    init {
        // Initialize the list in ViewModel
        loadImages()
    }

    // Function to add images to the list
    private fun loadImages() {
        val images = mutableListOf<ViewpagerModel>(
            ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.ic_image_for_viewpager),
            ViewpagerModel(R.drawable.image_hotel),
            ViewpagerModel(R.drawable.image_hotel),
            ViewpagerModel(R.drawable.image_hotel)
        )
        _imageList.value = images
    }


}