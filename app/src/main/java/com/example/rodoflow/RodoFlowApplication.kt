package com.example.rodoflow

import android.app.Application

class RodoFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.init(this)
    }
}
