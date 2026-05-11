package com.example.rodoflow.data.api

import com.example.rodoflow.AppLog
import okhttp3.Interceptor

/** Log temporário: URL completa de cada request. */
class RetrofitUrlLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        AppLog.d("RETROFIT_URL", request.url.toString())
        return chain.proceed(request)
    }
}
