package com.business.zyvo.fragment.both.browseArticleHost.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

    @HiltViewModel
    class BrowseArticleHostViewModel @Inject constructor(
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

        suspend fun getGuideList(search_term: String):
                Flow<NetworkResult<JsonObject>> {
            return repository.getGuideList(search_term).onEach {
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

        suspend fun getArticleList(search_term : String):
                Flow<NetworkResult<JsonObject>> {
            return repository.getArticleList(search_term).onEach {
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
    }