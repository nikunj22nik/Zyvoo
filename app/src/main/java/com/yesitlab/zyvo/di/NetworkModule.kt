package com.yesitlab.zyvo.di

import com.yesitlab.zyvo.remote.ZyvoApi
import com.yesitlab.zyvo.repository.ZyvoRepository
import com.yesitlab.zyvo.repository.ZyvoRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

//enter valid url
const val ZYVO_API_BASE_URL = "https://circleitadmin.tgastaging.com/api/"

@Module
@InstallIn(SingletonComponent::class)
object  NetworkModule {

    @Singleton
    @Provides
    fun provideCircleItApi(retrofit: Retrofit.Builder, okHttpClient: OkHttpClient): ZyvoApi {
        return  retrofit.client(okHttpClient).build().create(ZyvoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVoolayVooUserRepository(api:ZyvoApi): ZyvoRepository {
        return ZyvoRepositoryImpl(api)
    }



    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            //  .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder = Retrofit.Builder()
        .baseUrl(ZYVO_API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())


}