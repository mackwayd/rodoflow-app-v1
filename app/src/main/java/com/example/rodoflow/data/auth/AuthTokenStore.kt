package com.example.rodoflow.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.authDataStore by preferencesDataStore(name = "auth")

object AuthTokenStore {

    private val accessTokenKey = stringPreferencesKey("access_token")

    @Volatile
    var accessToken: String? = null
        private set

    suspend fun hydrateFromDisk(context: Context) {
        accessToken = context.authDataStore.data.first()[accessTokenKey]
    }

    suspend fun save(context: Context, token: String) {
        context.authDataStore.edit { prefs ->
            prefs[accessTokenKey] = token
        }
        accessToken = token
    }

    suspend fun clear(context: Context) {
        context.authDataStore.edit { prefs ->
            prefs.remove(accessTokenKey)
        }
        accessToken = null
    }
}
