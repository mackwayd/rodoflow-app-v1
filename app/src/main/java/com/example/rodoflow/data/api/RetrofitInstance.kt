package com.example.rodoflow.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Emulador: 10.0.2.2 = host. Físico: IP da máquina. Trailing slash obrigatório. Sem /api no @GET.
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(RetrofitUrlLoggingInterceptor())
            .addInterceptor(BearerTokenInterceptor())
            .build()
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
