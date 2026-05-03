package com.example.rodoflow

import android.app.Application
import android.content.Context

object AppContext {

    lateinit var applicationContext: Context
        private set

    fun init(app: Application) {
        applicationContext = app.applicationContext
    }
}
