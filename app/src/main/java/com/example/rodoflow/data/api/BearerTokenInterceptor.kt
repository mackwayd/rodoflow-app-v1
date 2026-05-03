package com.example.rodoflow.data.api

import com.example.rodoflow.data.auth.AuthTokenStore
import okhttp3.Interceptor

class BearerTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val path = request.url().encodedPath()
        if (path.endsWith("/auth/login")) {
            return chain.proceed(request)
        }
        val token = AuthTokenStore.accessToken
        val builder = request.newBuilder()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}
