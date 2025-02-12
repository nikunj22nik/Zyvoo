package com.business.zyvo.fragment.guest.termAndCondition.viewModel

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
class TermsViewModel @Inject constructor(
    private val repository: ZyvoRepository,
    val networkMonitor: NetworkMonitor
) : ViewModel() {
    val isLoading = MutableLiveData<Boolean>()

    suspend fun getTermCondition():
            Flow<NetworkResult<String>> {
        return repository.getTermCondition().onEach {
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