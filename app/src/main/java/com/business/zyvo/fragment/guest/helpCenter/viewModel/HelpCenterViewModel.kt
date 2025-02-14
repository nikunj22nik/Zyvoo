package com.business.zyvo.fragment.guest.helpCenter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.model.AllArticlesModel
import com.business.zyvo.model.AllGuidesModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HelpCenterViewModel @Inject constructor(
    private val repository: ZyvoRepository,
    val networkMonitor: NetworkMonitor
) : ViewModel() {
/*
    private val _list = MutableLiveData<ArrayList<AllGuidesModel>>()
    val list : LiveData<ArrayList<AllGuidesModel>> get() = _list

    private val _articlesList = MutableLiveData<ArrayList<AllArticlesModel>>()
    val articlesList : LiveData<ArrayList<AllArticlesModel>> get() =  _articlesList


 */

        val isLoading = MutableLiveData<Boolean>()

        suspend fun getHelpCenter(user_id : String,user_type : String):
                Flow<NetworkResult<JsonObject>> {
            return repository.getHelpCenter(user_id, user_type).onEach {
                when (it) {
                    is NetworkResult.Loading -> {
                        isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        isLoading.value = false
                    }

                    else -> {
                        isLoading.value = false
                    }
                }
            }
        }

    /*
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

     */

}