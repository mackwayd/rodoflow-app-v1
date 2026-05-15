package com.example.rodoflow.data.api

import com.example.rodoflow.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Emulador: 10.0.2.2 = host. Físico: IP da máquina. Trailing slash obrigatório para Retrofit.
    private const val BASE_URL = "http://186.208.159.187:8082/"

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
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
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
