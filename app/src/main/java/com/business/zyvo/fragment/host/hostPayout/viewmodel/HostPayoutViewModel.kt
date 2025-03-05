package com.business.zyvo.fragment.host.hostPayout.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
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

        suspend fun addPayOut(
            userId: RequestBody, firstName: RequestBody, lastName: RequestBody, email: RequestBody, phoneNumber: RequestBody,
            dobList: List<MultipartBody.Part>, idType: RequestBody, ssnLast4: RequestBody, idNumber: RequestBody,
            address: RequestBody, country: RequestBody, state: RequestBody, city: RequestBody, postalCode: RequestBody,
            bankName: RequestBody, accountHolderName: RequestBody, accountNumber: RequestBody, accountNumberConfirmation: RequestBody,
            routingProperty: RequestBody, bank_proof_document: MultipartBody.Part?, verification_document_front: MultipartBody.Part?,
            verification_document_back: MultipartBody.Part?
        ):
                Flow<NetworkResult<String>> {
            return repository.addPayOut(userId, firstName, lastName, email, phoneNumber, dobList, idType, ssnLast4, idNumber, address, country, state, city, postalCode, bankName, accountHolderName, accountNumber, accountNumberConfirmation, routingProperty, bank_proof_document, verification_document_front, verification_document_back).onEach {
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
