package com.example.rodoflow.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.AppServices
import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.data.sync.SyncScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingSyncViewModel : ViewModel() {
    private val store = AppServices.pendingOperationStore
    private val operations = AppServices.outgoingOperations

    val items: StateFlow<List<PendingOperationEntity>> = store.observeAll()
        .map { list -> list.sortedByDescending { it.createdAt } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun syncNow(context: android.content.Context) {
        viewModelScope.launch {
            operations.retryFailedAndSchedule()
            SyncScheduler.schedule(context)
        }
    }

    fun discard(id: String) {
        viewModelScope.launch {
            operations.discardPendingOperation(id)
        }
    }
}
