package com.business.zyvo.fragment.guest.profile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.AddPaymentCardModel
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.utils.NetworkMonitor
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import retrofit2.http.Field
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ZyvoRepository,
    val networkMonitor: NetworkMonitor
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()


//    private val _paymentCardList = MutableLiveData<MutableList<AddPaymentCardModel>>()
//    val paymentCardList: LiveData<MutableList<AddPaymentCardModel>> get() = _paymentCardList
//

//    init {
//        loadPaymentDetail()
//    }


    suspend fun updatePhoneNumber(
        userId: Int,
        phoneNumber: String,
        countryCode: String
    ): Flow<NetworkResult<String>> {
        return repository.updatePhoneNumber(userId, phoneNumber, countryCode).onEach {
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

    suspend fun otpVerifyUpdatePhoneNumber(

        userId: Int,
        otp: String
    ): Flow<NetworkResult<String>> {

       return repository.otpVerifyUpdatePhoneNumber(userId, otp).onEach {
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

    suspend fun otpResetPassword(userId: Int): Flow<NetworkResult<Pair<String, String>>> {
        return repository.otpResetPassword(userId).onEach {
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


//    private fun loadPaymentDetail() {
//        val paymentList = mutableListOf<AddPaymentCardModel>(
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888"),
//            AddPaymentCardModel("...458888")
//        )
//
//        _paymentCardList.value = paymentList
//
//    }


    suspend fun updateEmail(
        @Field("user_id") userId: Int,
        @Field("email") email: String
    ): Flow<NetworkResult<String>> {
        return repository.updateEmail(userId, email).onEach {
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

    suspend fun uploadProfileImage(userId: String, bytes: ByteArray):
            Flow<NetworkResult<Triple<String, String, String>>> {
        return repository.uploadProfileImage(userId, bytes).onEach {
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


    suspend fun phoneVerificationProfile(userId :String,code :String,number:String):
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

    suspend fun otpVerifyPhoneVerificationProfile(userId :String,otp :String):
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
    suspend fun otpVerifyEmailVerificationProfile(userId :String,otp :String):
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


    suspend fun otpVerifyUpdateEmail(
        userId: Int,
        otp: String
    ): Flow<NetworkResult<String>> {
        return repository.otpVerifyUpdateEmail(userId,otp).onEach {
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

    suspend fun addUpdateName(userId: String, first_name: String, last_name: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addUpdateName(userId, first_name, last_name).onEach {
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


    suspend fun addAboutMe(userId: String, about_me: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addAboutMe(
            userId,
            about_me
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

    suspend fun addLivePlace(userId: String, place_name: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addLivePlace(
            userId,
            place_name
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


    suspend fun deleteLivePlaceApi(userId: String, index: Int):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.deleteLivePlace(userId, index).onEach {
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

    suspend fun addMyWorkApi(userId: String, workName: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addMyWork(userId, workName).onEach {

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

    suspend fun deleteMyWorkApi(userId: String, index: Int):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.deleteMyWork(userId, index).onEach {
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

    suspend fun addHobbiesApi(userId: String, hobbies_name: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addHobbies(userId, hobbies_name).onEach {
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

    suspend fun deleteHobbiesApi(userId: String, index: Int):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.deleteHobbies(userId, index).onEach {
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

    suspend fun addPetApi(userId: String, pet_name: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addPets(userId, pet_name).onEach {
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

    suspend fun deletePetsApi(userId: String, index: Int):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.deletePets(userId, index).onEach {
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

    suspend fun addStreetAddressApi(userId: String, street_address: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addStreetAddress(userId, street_address).onEach {
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

    suspend fun addCityApi(userId: String, city: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addCity(userId, city).onEach {
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

    suspend fun addStateApi(userId: String, state: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addState(userId, state).onEach {
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

    suspend fun addZipCodeApi(userId: String, zip_code: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.addZipCode(userId, zip_code).onEach {
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

    suspend fun updatePasswordApi(userId: String, password: String, password_confirmation: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.updatePassword(userId, password, password_confirmation).onEach {
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
    suspend fun emailVerificationProfile(userId: String,email :String):
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

    suspend fun emailVerification(userId: String, email: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.emailVerification(userId, email).onEach {
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

    suspend fun phoneVerification(userId: String, code: String, number: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.phoneVerification(userId, code, number).onEach {
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

    suspend fun otpVerifyPhoneVerification(userId: String, otp: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.otpVerifyPhoneVerification(userId, otp).onEach {
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

    suspend fun otpVerifyEmailVerification(userId: String, otp: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.otpVerifyEmailVerification(userId, otp).onEach {
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


    suspend fun getVerifyIdentityApi(userId: String, identity_verify: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.verifyIdentity(userId, identity_verify).onEach {
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

    suspend fun logout(userId: String):
            Flow<NetworkResult<String>> {
        return repository.logout(userId).onEach {
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

    suspend fun getUserCards(userId: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.getUserCards(userId).onEach {
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

    suspend fun getPayoutMethods(userId: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.getPayoutMethods(userId).onEach {
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

    suspend fun saveCardStripe(userId: String, token_stripe: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.saveCardStripe(userId, token_stripe).onEach {
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

    suspend fun sameAsMailingAddress(userId: String):
            Flow<NetworkResult<JsonObject>> {
        return repository.sameAsMailingAddress(userId).onEach {

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

    suspend fun setPrimaryPayoutMethod(userId: String, payoutMethodId: String):
            Flow<NetworkResult<String>> {
        return repository.setPrimaryPayoutMethod(userId, payoutMethodId).onEach {
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

    suspend fun deletePayoutMethod(userId: String, payoutMethodId: String):
            Flow<NetworkResult<String>> {
        return repository.deletePayoutMethod(userId, payoutMethodId).onEach {
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


    suspend fun setPreferredCard(userId: String, card_id: String):
            Flow<NetworkResult<Pair<String, String>>> {
        return repository.setPreferredCard(userId, card_id).onEach {

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

    suspend fun deleteCard( userId: String, paymentMethodId: String):
            Flow<NetworkResult<String>>{
        return repository.deleteCard(userId,paymentMethodId).onEach {
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