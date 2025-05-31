package com.business.zyvo.fragment.guest.privacy.viewModel

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
    class PrivacyPolicyViewModel  @Inject constructor(private val repository: ZyvoRepository,
                                                val networkMonitor: NetworkMonitor
    ) : ViewModel(){
        val isLoading = MutableLiveData<Boolean>()

        suspend fun getPrivacyPolicy():
                Flow<NetworkResult<Pair<String,String>>> {
            return repository.getPrivacyPolicy().onEach {
                when(it){
                    is NetworkResult.Loading -> {
                        isLoading.value = true
                    } is NetworkResult.Success -> {
                    isLoading.value = false
                } else -> {
                    isLoading.value = false
                }
                }
            }
        }
    }