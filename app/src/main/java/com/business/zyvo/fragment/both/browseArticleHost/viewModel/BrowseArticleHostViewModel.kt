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

        val isLoading = MutableLiveData<Boolean>()

        suspend fun getGuideList(search_term: String,user_type:String):
                Flow<NetworkResult<JsonObject>> {
            return repository.getGuideList(search_term,user_type).onEach {
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

        suspend fun getArticleList(search_term : String,user_type: String):
                Flow<NetworkResult<JsonObject>> {
            return repository.getArticleList(search_term,user_type).onEach {
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