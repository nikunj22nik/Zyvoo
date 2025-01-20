package com.business.zyvo.repository

import com.business.zyvo.remote.ZyvoApi
import javax.inject.Inject

class ZyvoRepositoryImpl @Inject constructor(private val api:ZyvoApi):ZyvoRepository {

}