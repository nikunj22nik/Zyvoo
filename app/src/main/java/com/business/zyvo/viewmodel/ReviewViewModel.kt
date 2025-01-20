package com.business.zyvo.viewmodel

import androidx.lifecycle.ViewModel
import com.business.zyvo.repository.ZyvoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(private val repository: ZyvoRepository) : ViewModel(){
  //  private  var _list = MutableLiveData<<>>


}