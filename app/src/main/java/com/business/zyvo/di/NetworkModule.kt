package com.business.zyvo.di

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.business.zyvo.BuildConfig
import com.business.zyvo.remote.ZyvoApi
import com.business.zyvo.repository.ZyvoRepository
import com.business.zyvo.repository.ZyvoRepositoryImpl
import com.business.zyvo.utils.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object  NetworkModule {

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


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
    fun provideAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor =
        AuthInterceptor(context)



    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message -> Log.d("RetrofitLog", message) }
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }else{
            loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60,java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())


}