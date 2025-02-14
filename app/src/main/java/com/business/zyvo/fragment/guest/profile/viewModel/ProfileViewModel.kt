package com.business.zyvo.fragment.guest.profile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.AddPaymentCardModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel  @Inject constructor(private val repository: ZyvoRepository,
                                            val networkMonitor: NetworkMonitor) : ViewModel(){

    val isLoading = MutableLiveData<Boolean>()

    private val _paymentCardList = MutableLiveData<MutableList<AddPaymentCardModel>>()
    val paymentCardList : LiveData<MutableList<AddPaymentCardModel>> get() =  _paymentCardList


    init {
        loadPaymentDetail()
    }



    private fun loadPaymentDetail(){
        val paymentList = mutableListOf<AddPaymentCardModel>(
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888"),
            AddPaymentCardModel("...458888")
        )

        _paymentCardList.value = paymentList

    }

    suspend fun uploadProfileImage(userId: String,bytes: ByteArray):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.uploadProfileImage(userId,bytes).onEach {
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


    suspend fun addAboutMe(userId: String,about_me: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addAboutMe(userId,
            about_me).onEach {
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

    suspend fun addLivePlace(userId: String,place_name: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addLivePlace(userId,
            place_name).onEach {

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


    suspend fun deleteLivePlaceApi(userId: String,index: Int):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.deleteLivePlace(userId,index ).onEach {
            when(it){
                is NetworkResult.Loading -> {
                    isLoading.value = true
                }
                is NetworkResult.Success -> {
                isLoading.value = false
            } else -> {
                isLoading.value = false
            }
            }
        }
    }

    suspend fun addMyWorkApi(userId: String, workName: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addMyWork(userId,workName ).onEach {

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
    suspend fun deleteMyWorkApi(userId: String,index: Int):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.deleteMyWork(userId,index ).onEach {
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

    suspend fun addLanguageApi(userId: String,language: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addLanguage(userId,language ).onEach {
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

    suspend fun deleteLanguageApi(userId: String,index: Int):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.deleteLanguage(userId,index ).onEach {
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

    suspend fun addHobbiesApi(userId: String,hobbies_name: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addHobbies(userId,hobbies_name ).onEach {
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

    suspend fun deleteHobbiesApi(userId: String,index: Int):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.deleteHobbies(userId,index ).onEach {
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

    suspend fun addPetApi(userId: String,pet_name: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addPets(userId,pet_name ).onEach {
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

    suspend fun deletePetsApi(userId: String,index: Int):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.deletePets(userId,index ).onEach {
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

    suspend fun addStreetAddressApi(userId: String,street_address: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addStreetAddress(userId,street_address ).onEach {
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

    suspend fun addCityApi(userId: String,city: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addCity(userId,city ).onEach {
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

    suspend fun addStateApi(userId: String,state: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addState(userId,state ).onEach {
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

    suspend fun addZipCodeApi(userId: String,zip_code: String):
            Flow<NetworkResult<Pair<String,String>>> {
        return repository.addZipCode(userId,zip_code ).onEach {
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