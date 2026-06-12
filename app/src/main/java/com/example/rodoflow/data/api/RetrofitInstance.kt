package com.example.rodoflow.data.api

import com.example.rodoflow.AppServices
import com.example.rodoflow.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val gson = GsonBuilder()
        .registerTypeAdapter(Double::class.java, FlexibleDoubleAdapter())
        .registerTypeAdapter(java.lang.Double.TYPE, FlexibleDoubleAdapter())
        .create()

    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = AppServices.authSession.currentToken()
                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                } else if (BuildConfig.DEBUG) {
                    requestBuilder.header("X-RodoFlow-Api-Key", BuildConfig.RODOFLOW_API_KEY)
                }
                chain.proceed(requestBuilder.build())
            }
        if (BuildConfig.DEBUG) {
            builder
                .addInterceptor(RetrofitUrlLoggingInterceptor())
                .addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
                )
        }
        builder.build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.RODOFLOW_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
