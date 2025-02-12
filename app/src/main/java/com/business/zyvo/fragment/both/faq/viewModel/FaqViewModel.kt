package com.business.zyvo.fragment.both.faq.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.fragment.both.faq.model.FaqModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


    @HiltViewModel
    class FaqViewModel @Inject constructor(
        private val repository: ZyvoRepository,
        val networkMonitor: NetworkMonitor
    ) : ViewModel() {
        val isLoading = MutableLiveData<Boolean>()

        suspend fun getFaq():
                Flow<NetworkResult<MutableList<FaqModel>>> {
            return repository.getFaq().onEach {
                when (it) {
                    is NetworkResult.Loading -> {
                        Log.d("****","startLoading")
                        isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        isLoading.value = false
                        Log.d("****","stopLoading")
                    }

                    else -> {
                        isLoading.value = false
                        Log.d("****","stopLoading")
                    }
                }
            }
        }
}