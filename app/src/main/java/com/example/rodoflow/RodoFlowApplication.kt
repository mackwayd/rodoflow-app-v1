package com.example.rodoflow

import android.app.Application
import com.example.rodoflow.data.auth.AuthTokenStore
import kotlinx.coroutines.runBlocking

class RodoFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.init(this)
        runBlocking {
            AuthTokenStore.hydrateFromDisk(this@RodoFlowApplication)
        }
    }
}
