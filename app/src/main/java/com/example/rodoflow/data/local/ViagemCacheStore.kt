package com.example.rodoflow.data.local

import android.content.Context
import com.example.rodoflow.data.model.Viagem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class ViagemCacheStore(context: Context) {
    private val gson = Gson()
    private val dir = File(context.filesDir, "viagem_cache").apply { mkdirs() }
    private val listFile = File(dir, "viagens.json")
    private val mutex = Mutex()

    suspend fun saveViagens(viagens: List<Viagem>) = mutex.withLock {
        listFile.writeText(gson.toJson(viagens))
    }

    suspend fun getViagens(): List<Viagem>? = mutex.withLock {
        if (!listFile.exists()) return@withLock null
        val json = listFile.readText()
        if (json.isBlank()) return@withLock null
        val type = object : TypeToken<List<Viagem>>() {}.type
        runCatching { gson.fromJson<List<Viagem>>(json, type) }.getOrNull()
    }

    suspend fun saveViagemDetail(viagem: Viagem) = mutex.withLock {
        File(dir, "detail_${viagem.id}.json").writeText(gson.toJson(viagem))
    }

    suspend fun getViagemDetail(id: String): Viagem? = mutex.withLock {
        val file = File(dir, "detail_$id.json")
        if (!file.exists()) return@withLock null
        runCatching { gson.fromJson(file.readText(), Viagem::class.java) }.getOrNull()
    }
}
