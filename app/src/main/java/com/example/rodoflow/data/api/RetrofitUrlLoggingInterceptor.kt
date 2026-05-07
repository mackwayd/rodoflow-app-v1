package com.example.rodoflow.data.api

import android.util.Log
import okhttp3.Interceptor

/** Log temporário: URL completa de cada request. */
class RetrofitUrlLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        Log.d("RETROFIT_URL", request.url.toString())
        return chain.proceed(request)
    }
}
