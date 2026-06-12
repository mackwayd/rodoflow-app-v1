package com.example.rodoflow.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rodoflow.AppServices
import com.example.rodoflow.data.local.entity.PendingOperationEntity

class SyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val store = AppServices.pendingOperationStore
        val engine = AppServices.syncEngine
        val comprovanteStorage = AppServices.comprovanteStorage

        val pending = store.getByStatus(PendingOperationEntity.STATUS_PENDING)
        if (pending.isEmpty()) return Result.success()

        for (operation in pending) {
            try {
                engine.execute(operation)
                store.deleteById(operation.id)
                comprovanteStorage.delete(operation.comprovantePath)
            } catch (e: NonRetriableSyncException) {
                store.updateStatus(
                    operation.id,
                    PendingOperationEntity.STATUS_FAILED,
                    e.userMessage,
                )
            } catch (e: Exception) {
                return Result.retry()
            }
        }
        return Result.success()
    }
}
