package com.business.zyvo.fragment.guest.feedback.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

 @HiltViewModel
    class FeedbackViewModel @Inject constructor(
        private val repository: ZyvoRepository,
        val networkMonitor: NetworkMonitor
    ) : ViewModel() {
        val isLoading = MutableLiveData<Boolean>()

        suspend fun feedback(user_id : String,type: String,details: String):
                Flow<NetworkResult<String>> {
            return repository.feedback(user_id, type, details).onEach {
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