package com.example.rodoflow

import android.app.Application
import com.example.rodoflow.data.auth.AuthSessionStore
import com.example.rodoflow.data.local.PendingOperationStore
import com.example.rodoflow.data.repository.AuthRepository
import com.example.rodoflow.data.local.ViagemCacheStore
import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.data.repository.OutgoingOperationsRepository
import com.example.rodoflow.data.repository.ViagemRepository
import com.example.rodoflow.data.sync.ComprovanteStorage
import com.example.rodoflow.data.sync.SyncEngine
import com.example.rodoflow.data.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AppServices {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var application: Application

    val authSession: AuthSessionStore by lazy {
        AuthSessionStore(application.applicationContext)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    val pendingOperationStore: PendingOperationStore by lazy {
        PendingOperationStore(application.applicationContext)
    }

    val viagemCache: ViagemCacheStore by lazy {
        ViagemCacheStore(application.applicationContext)
    }

    val viagemRepository: ViagemRepository by lazy {
        ViagemRepository(
            cache = viagemCache,
            pendingStore = pendingOperationStore,
        )
    }

    val comprovanteStorage: ComprovanteStorage by lazy {
        ComprovanteStorage(application.applicationContext)
    }

    val syncEngine: SyncEngine by lazy {
        SyncEngine(viagemRepository, comprovanteStorage)
    }

    val outgoingOperations: OutgoingOperationsRepository by lazy {
        OutgoingOperationsRepository(
            context = application.applicationContext,
            viagemRepository = viagemRepository,
            pendingStore = pendingOperationStore,
            comprovanteStorage = comprovanteStorage,
        )
    }

    fun init(app: Application) {
        application = app
        scope.launch {
            val pending = pendingOperationStore
                .getByStatus(PendingOperationEntity.STATUS_PENDING)
            if (pending.isNotEmpty()) {
                SyncScheduler.schedule(app.applicationContext)
            }
        }
    }
}
