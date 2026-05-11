package com.example.rodoflow

import android.util.Log

/** Logs de depuração: silenciados em builds release (BuildConfig.DEBUG = false). */
object AppLog {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }
}
