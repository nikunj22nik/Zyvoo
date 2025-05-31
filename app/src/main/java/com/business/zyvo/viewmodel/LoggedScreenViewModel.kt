package com.business.zyvo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.LogModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoggedScreenViewModel @Inject constructor(
    private val repository: ZyvoRepository,
    val networkMonitor: NetworkMonitor
) : ViewModel() {
    val isLoading = MutableLiveData<Boolean>()

    /*fun signupPhoneNumber(code:String,number :String){
          viewModelScope.launch {
             val result = repository.signUpPhoneNumber(code, number)
             phoneSignUpLiveData.value = result
          }
   }*/
    suspend fun signupPhoneNumber(code: String, number: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.signUpPhoneNumber(number, code).onEach {
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

    suspend fun signupEmail(
        email: String,
        password: String
    ):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.signupEmail(email, password).onEach {
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


    suspend fun loginPhoneNumber(code: String, number: String,fcmToken: String): Flow<NetworkResult<Pair<String, String>>> {
        return repository.loginPhoneNumber(number, code,fcmToken).onEach {
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

    suspend fun otpVerifyLoginPhone(userId: String, otp: String,fcmToken :String):
            Flow<NetworkResult<JsonObject>> {
        return repository.otpVerifyLoginPhone(userId, otp,fcmToken).onEach {
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

    suspend fun otpVerifySignupPhone(tempId: String, otp: String,fcmToken :String):
            Flow<NetworkResult<JsonObject>> {
        return repository.otpVerifySignupPhone(tempId, otp,fcmToken).onEach {
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

    suspend fun loginEmail(email: String, password: String,fcmToken :String):
            Flow<NetworkResult<JsonObject>> {
        return repository.loginEmail(email, password,fcmToken).onEach {
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

    suspend fun otpVerifySignupEmail(tempId: String, otp: String,fcmToken :String):
            Flow<NetworkResult<JsonObject>> {
        return repository.otpVerifySignupEmail(tempId, otp,fcmToken).onEach {
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

    suspend fun forgotPassword(email: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.forgotPassword(email).onEach {
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

    suspend fun otpVerifyForgotPassword(userId: String, otp: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.otpVerifyForgotPassword(userId, otp).onEach {
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

    suspend fun resetPassword(
        userId: String, password: String,
        passwordConfirmation: String
    ):
            Flow<NetworkResult<JsonObject>> {
        return repository.resetPassword(userId, password, passwordConfirmation).onEach {
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


    suspend fun getSocialAPI(
        fname: String,
        lname: String,
        email: String,
        social_id: String,
        fcm_token: String,
        device_type: String
    ): Flow<NetworkResult<JsonObject>> {
        return repository.getSocialLogin(fname, lname, email, social_id, fcm_token, device_type)
            .onEach {
                when (it) {
                    is NetworkResult.Loading -> {
                        isLoading.postValue(true)
                    }

                    is NetworkResult.Success -> {
                        isLoading.postValue(false)
                    }

                    else -> {
                        isLoading.postValue(false)

                    }
                }
            }
    }

    suspend fun getHomeData(
        userId: String,
        latitude: String,
        longitude: String):
            Flow<NetworkResult<JsonArray>> {
        return repository.getHomeData(
            userId,
            latitude,
            longitude
        ).onEach {
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

    suspend fun getFilterHomeDataApi( userId: String?, latitude: String?, longitude: String?, place_type: String?, minimum_price: String?, maximum_price: String?,
                                      location: String?, date: String?, time: String?, people_count: String?, property_size: String?, bedroom: String?, bathroom: String?, instant_booking: String?,
                                      self_check_in: String?, allows_pets: String?, activities: List<String>?, amenities: List<String>?, languages: List<String>?): Flow<NetworkResult<JsonArray>> {
        return repository.getFilteredHomeData(userId,latitude,longitude,place_type,minimum_price,maximum_price,
            location,date,time,people_count,property_size,bedroom,bathroom,instant_booking,self_check_in,allows_pets,activities,amenities,languages).onEach {
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

    suspend fun getHomeDataSearchFilter(
        user_id : String,
        latitude : String,
        longitude : String,
        date : String,
        hour : String,
        start_time : String,
        end_time : String,
        activity : String):
            Flow<NetworkResult<JsonArray>> {
        return repository.getHomeDataSearchFilter(user_id, latitude, longitude,date,
            hour,start_time, end_time, activity).onEach {
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
