package com.yesitlab.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.model.WishListModel
import com.yesitlab.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class WishlistViewModel @Inject constructor(private var repository: ZyvoRepository): ViewModel(){
    private var _list = MutableLiveData<MutableList<WishListModel>>()
    val list : LiveData<MutableList<WishListModel>> get() = _list

    init {
        load()
    }

    private fun load() {
      val listItem = mutableListOf<WishListModel>(
          WishListModel(R.drawable.ic_image_for_viewpager,"Cabin in Peshastin", "4 saved"),
          WishListModel(R.drawable.ic_image_for_viewpager,"Cabin in Peshastin", "4 saved"),
          WishListModel(R.drawable.ic_image_for_viewpager,"Cabin in Peshastin", "4 saved"),
          WishListModel(R.drawable.ic_image_for_viewpager,"Cabin in Peshastin", "4 saved"),
          WishListModel(R.drawable.ic_image_for_viewpager,"Cabin in Peshastin", "4 saved"),
      )

        _list.value = listItem
    }
}