package com.example.rodoflow.data.local

import android.content.Context
import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class PendingOperationStore(context: Context) {
    private val gson = Gson()
    private val file = File(context.filesDir, "pending_operations.json")
    private val mutex = Mutex()
    private val operations = MutableStateFlow(readAllFromDisk())

    fun observeAll(): Flow<List<PendingOperationEntity>> = operations

    fun observeCountByStatus(status: String): Flow<Int> =
        operations.map { list -> list.count { it.status == status } }

    suspend fun findById(id: String): PendingOperationEntity? = mutex.withLock {
        operations.value.find { it.id == id }
    }

    suspend fun getByStatus(status: String): List<PendingOperationEntity> = mutex.withLock {
        operations.value.filter { it.status == status }.sortedBy { it.createdAt }
    }

    suspend fun insert(entity: PendingOperationEntity) = mutex.withLock {
        val list = operations.value.toMutableList()
        list.removeAll { it.id == entity.id }
        list.add(entity)
        persist(list)
    }

    suspend fun deleteById(id: String) = mutex.withLock {
        persist(operations.value.filterNot { it.id == id })
    }

    suspend fun updateStatus(id: String, status: String, lastError: String?) = mutex.withLock {
        persist(
            operations.value.map { op ->
                if (op.id == id) op.copy(status = status, lastError = lastError) else op
            },
        )
    }

    suspend fun resetFailedToPending() = mutex.withLock {
        persist(
            operations.value.map { op ->
                if (op.status == PendingOperationEntity.STATUS_FAILED) {
                    op.copy(status = PendingOperationEntity.STATUS_PENDING, lastError = null)
                } else {
                    op
                }
            },
        )
    }

    private fun persist(list: List<PendingOperationEntity>) {
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(list))
        operations.value = list
    }

    private fun readAllFromDisk(): List<PendingOperationEntity> {
        if (!file.exists()) return emptyList()
        val json = file.readText()
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<PendingOperationEntity>>() {}.type
        return runCatching { gson.fromJson<List<PendingOperationEntity>>(json, type) }.getOrDefault(emptyList())
    }
}
