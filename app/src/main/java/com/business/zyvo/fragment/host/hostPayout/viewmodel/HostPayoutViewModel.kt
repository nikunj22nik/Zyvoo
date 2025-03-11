package com.business.zyvo.fragment.host.hostPayout.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.StateModel
import com.business.zyvo.model.host.CountryModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


@HiltViewModel
    class HostPayoutViewModel @Inject constructor(
        private val repository: ZyvoRepository,
        val networkMonitor: NetworkMonitor
    ) : ViewModel() {

        val isLoading = MutableLiveData<Boolean>()
        val countriesList = mutableListOf<String>()
        val stateList = mutableListOf<String>()
        val countryList = mutableListOf<String>()

        suspend fun addPayOut(
            userId: RequestBody,
            firstName: RequestBody,
            lastName: RequestBody,
            email: RequestBody,
            phoneNumber: RequestBody,
            dobList: List<MultipartBody.Part>,
            idType: RequestBody,
            ssnLast4: RequestBody,
            idNumber: RequestBody,
            address: RequestBody,
            country: RequestBody,
            state: RequestBody,
            city: RequestBody,
            postalCode: RequestBody,
            bankName: RequestBody,
            accountHolderName: RequestBody,
            accountNumber: RequestBody,
            accountNumberConfirmation: RequestBody,
            routingProperty: RequestBody,
            bankDocumentTypeBody: RequestBody,
            bank_proof_document: MultipartBody.Part?,
            verification_document_front: MultipartBody.Part?,
            verification_document_back: MultipartBody.Part?

        ):
                Flow<NetworkResult<String>> {
            return repository.addPayOut(userId, firstName, lastName, email, phoneNumber, dobList, idType, ssnLast4, idNumber, address, country, state, city, postalCode, bankName, accountHolderName, accountNumber, accountNumberConfirmation, routingProperty,bankDocumentTypeBody, bank_proof_document, verification_document_front, verification_document_back).onEach {
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

     suspend fun getCountries(): Flow<NetworkResult<MutableList<CountryModel>>>{

         return repository.getCountries().onEach {
             when(it){
                 is NetworkResult.Success ->{
                     countriesList.clear()

                 }
                 is NetworkResult.Error ->{

                 }
                 else ->{

                 }
             }

         }

    }

    fun getCountriesList(list:MutableList<CountryModel>) :MutableList<String> {
        list?.forEach {
            countriesList.add(it.name)
        }
        return countriesList
    }

    suspend fun getState( value: String) : Flow<NetworkResult<MutableList<StateModel>>>{
        return repository.getState(value).onEach {
          when(it){
              is NetworkResult.Success ->{

              }
              is NetworkResult.Error ->{

              }
              else ->{

              }
          }
        }
    }
    suspend fun getStateList(list:MutableList<StateModel>) : MutableList<String>{
        stateList.clear()
        list.forEach {
            stateList.add(it.name)
        }
        return stateList
    }

     suspend fun getCityName(country:String, state :String)  :Flow<NetworkResult<MutableList<String>>>{

         return repository.getCityName(country, state).onEach {

         }
     }





    suspend fun addPayOutCard(
        userId: RequestBody,
        token: RequestBody,
        firstName: RequestBody,
        lastName: RequestBody,
        email: RequestBody,
        dobList: List<MultipartBody.Part>,
        ssnLast4: RequestBody,
        phoneNumber: RequestBody,
        address: RequestBody,
        city: RequestBody,
        state: RequestBody,
        country: RequestBody,
        postalCode: RequestBody,
        idType: RequestBody,
        idNumber: RequestBody,
        verification_document_front: MultipartBody.Part?,
        verification_document_back: MultipartBody.Part?
    ): Flow<NetworkResult<String>> {
        return repository.addPayCard( userId,
            token,
            firstName,
            lastName,
            email,
            dobList,
            ssnLast4,
            phoneNumber,
            address,
            city,
            state,
            country,
            postalCode,
            idType,
            idNumber,
            verification_document_front,
            verification_document_back).onEach {
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
