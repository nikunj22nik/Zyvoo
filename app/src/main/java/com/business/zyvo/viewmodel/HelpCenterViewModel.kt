package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.R
import com.business.zyvo.model.AllArticlesModel
import com.business.zyvo.model.AllGuidesModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HelpCenterViewModel @Inject constructor(private val repository: ZyvoRepository) : ViewModel() {

    private val _list = MutableLiveData<ArrayList<AllGuidesModel>>()
    val list : LiveData<ArrayList<AllGuidesModel>> get() = _list

    private val _articlesList = MutableLiveData<ArrayList<AllArticlesModel>>()
    val articlesList : LiveData<ArrayList<AllArticlesModel>> get() =  _articlesList

    init {
        load()
    }

    private fun load() {
       val listItem = arrayListOf<AllGuidesModel>(
           AllGuidesModel(R.drawable.image_hotel, "Cabin in Peshastin"),
           AllGuidesModel(R.drawable.image_hotel, "Payments"),
           AllGuidesModel(R.drawable.image_hotel, "Security & Safety"),
           AllGuidesModel(R.drawable.image_hotel, "Cancelations & Refunds"),
           AllGuidesModel(R.drawable.image_hotel, "Cabin in Peshastin"),
           AllGuidesModel(R.drawable.image_hotel, "Cabin in Peshastin"),
           AllGuidesModel(R.drawable.image_hotel, "Cabin in Peshastin"),
           AllGuidesModel(R.drawable.image_hotel, "Cabin in Peshastin"),
           AllGuidesModel(R.drawable.image_hotel, "Cabin in Peshastin"),
           AllGuidesModel(R.drawable.image_hotel, "Cabin in Peshastin"),

       )

        _list.value = listItem

        val articleItem = arrayListOf<AllArticlesModel>(
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has..."),
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has..."),
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has..."),
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has..."),
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has..."),
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has..."),
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has..."),
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has..."),
            AllArticlesModel("Article Topic Title","Lorem Ipsum is simply dummy text of the printing and typesetting industry. lorem has...")
        )

        _articlesList.value = articleItem

    }

//    // Method to remove item at a specific position
//    fun removeItemAt(position: Int) {
//        _list.value?.removeAt(position)
//        _list.value = _list.value // Trigger observer by setting the value again
//    }

}