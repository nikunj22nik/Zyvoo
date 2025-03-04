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
    // MutableLiveData to store the list of images
    private val _imageList = MutableLiveData<MutableList<LogModel>>()
    val imageList: LiveData<MutableList<LogModel>> get() = _imageList
    var phoneSignUpLiveData: MutableLiveData<NetworkResult<Pair<String, String>>> =
        MutableLiveData<NetworkResult<Pair<String, String>>>()
    val isLoading = MutableLiveData<Boolean>()

    init {
        // Initialize the list in ViewModel
        loadImages()
    }

    // Function to add images to the list
    private fun loadImages() {
        val images = mutableListOf<LogModel>(
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h"),
            LogModel("Cabin in Peshastin", "4.0", "(1k+)", "37 miles away", "\$12 / h")

        )
        _imageList.value = images
    }

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


    suspend fun loginPhoneNumber(code: String, number: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.loginPhoneNumber(number, code).onEach {
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

    suspend fun otpVerifyLoginPhone(userId: String, otp: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.otpVerifyLoginPhone(userId, otp).onEach {
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

    suspend fun otpVerifySignupPhone(tempId: String, otp: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.otpVerifySignupPhone(tempId, otp).onEach {
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

    suspend fun loginEmail(email: String, password: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.loginEmail(email, password).onEach {
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

    suspend fun otpVerifySignupEmail(tempId: String, otp: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.otpVerifySignupEmail(tempId, otp).onEach {
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

}
