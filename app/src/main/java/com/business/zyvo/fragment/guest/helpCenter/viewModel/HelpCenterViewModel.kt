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
}