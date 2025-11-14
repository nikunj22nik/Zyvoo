package com.business.zyvo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.business.zyvo.NetworkResult
import com.business.zyvo.model.ChannelListModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import retrofit2.http.Field
import javax.inject.Inject

@HiltViewModel
class GuestMainActivityModel @Inject constructor(private val repository: ZyvoRepository):
    ViewModel(){

    var chatChannel :MutableList<ChannelListModel> = mutableListOf()

    suspend fun getChatToken(userId :Int,role:String) : Flow<NetworkResult<String>>{
        return repository.getChatToken(userId, role).onEach {
          when(it){
              is NetworkResult.Success->{
                  Log.d("TESTING_TOKEN","Token in viewmodel"+it.data.toString())
              }
              is NetworkResult.Error ->{

              }
              else ->{

              }
          }
        }
    }

    suspend fun getChatToken1(userId :Int,role:String) : Flow<NetworkResult<String>>{
        return repository.getChatToken(userId, role).onEach {
            when(it){
                is NetworkResult.Success->{
                    Log.d("TESTING_TOKEN","Token in viewmodel"+it.data.toString())
                }
                is NetworkResult.Error ->{

                }
                else ->{

                }
            }
        }
    }

    suspend fun getChatUserChannelList(userId :Int,type :String) : Flow<NetworkResult<MutableList<ChannelListModel>>>{
        return repository.getUserChannel(userId,type,"").onEach {

        }
    }

    suspend fun getHostUnreadBookings(@Field("user_id") userId :Int) : Flow<NetworkResult<Int>>{
        return repository.getHostUnreadBookings(userId).onEach {
        }
    }
}