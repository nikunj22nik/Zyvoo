package com.business.zyvo.fragment.both.completeProfile.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.fragment.both.completeProfile.model.CompleteProfileReq
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(private val repository: ZyvoRepository,
                                                   val networkMonitor: NetworkMonitor
):
    ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    suspend fun deleteLanguageApi(userId: String, index: Int):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.deleteLanguage(userId, index).onEach {
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
    suspend fun emailVerification(userId: String,email :String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.emailVerification(userId, email).onEach {
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

    suspend fun phoneVerification(userId :String,code :String,number:String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.phoneVerification(userId, code,number).onEach {
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

    suspend fun otpVerifyEmailVerification(userId :String,otp :String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.otpVerifyEmailVerification(userId, otp).onEach {
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

    suspend fun otpVerifyPhoneVerification(userId :String,otp :String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.otpVerifyPhoneVerification(userId, otp).onEach {
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

    suspend fun completeProfile(completeProfileReq: CompleteProfileReq):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.completeProfile(completeProfileReq).onEach {
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

    suspend fun addUpdateName(userId: String,first_name: String, last_name: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addUpdateName(userId,first_name, last_name).onEach {
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

    suspend fun getUserProfile(userId: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.getUserProfile(userId).onEach {
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


    suspend fun addLanguageApi(userId: String, language_name: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addLanguage(userId, language_name).onEach {
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
