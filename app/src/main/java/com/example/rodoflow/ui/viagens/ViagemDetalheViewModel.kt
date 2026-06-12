package com.example.rodoflow.ui.viagens

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.rodoflow.AppLog
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.AppServices
import com.example.rodoflow.data.repository.OperationResult
import com.example.rodoflow.data.repository.ViagemDetailLoadResult
import com.example.rodoflow.data.sync.PendingSyncHints
import com.example.rodoflow.ui.util.userMessageForThrowable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ViagemDetalheViewModel(
    private val operations: com.example.rodoflow.data.repository.OutgoingOperationsRepository =
        AppServices.outgoingOperations,
    private val readRepository: com.example.rodoflow.data.repository.ViagemRepository =
        AppServices.viagemRepository,
) : ViewModel() {

    private val _viagem = MutableStateFlow<Viagem?>(null)
    val viagem: StateFlow<Viagem?> = _viagem.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _notFound = MutableStateFlow(false)
    val notFound: StateFlow<Boolean> = _notFound.asStateFlow()
    private val _offlineNoData = MutableStateFlow(false)
    val offlineNoData: StateFlow<Boolean> = _offlineNoData.asStateFlow()
    private val _fromCache = MutableStateFlow(false)
    val fromCache: StateFlow<Boolean> = _fromCache.asStateFlow()
    private val _isFinalizando = MutableStateFlow(false)
    val isFinalizando: StateFlow<Boolean> = _isFinalizando.asStateFlow()
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()
    private val _pendingFinalize = MutableStateFlow(false)
    val pendingFinalize: StateFlow<Boolean> = _pendingFinalize.asStateFlow()

    private var pendingObserveJob: Job? = null

    fun loadViagem(viagemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _notFound.value = false
            _offlineNoData.value = false
            _fromCache.value = false
            when (val result = readRepository.loadViagemById(viagemId)) {
                is ViagemDetailLoadResult.Success -> {
                    _viagem.value = result.viagem
                    _fromCache.value = result.fromCache
                }
                is ViagemDetailLoadResult.NotFound -> {
                    _viagem.value = null
                    _notFound.value = true
                }
                is ViagemDetailLoadResult.Error -> {
                    _viagem.value = null
                    _offlineNoData.value = true
                }
            }
            _isLoading.value = false
        }
        observePendingFinalize(viagemId)
    }

    private fun observePendingFinalize(viagemId: String) {
        pendingObserveJob?.cancel()
        pendingObserveJob = viewModelScope.launch {
            PendingSyncHints.observePendingFinalizeForViagem(
                AppServices.pendingOperationStore,
                viagemId,
            ).collect { _pendingFinalize.value = it }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    fun finalizarViagem(
        viagemId: String,
        teveQuebra: Boolean,
        kgPerdido: Double? = null,
        valorQuebra: Double? = null,
        observacaoQuebra: String? = null,
        onSuccess: (queued: Boolean) -> Unit = {},
    ) {
        viewModelScope.launch {
            _isFinalizando.value = true
            _actionError.value = null
            AppLog.d(
                "FINALIZAR_VIAGEM",
                "iniciando finalizar viagemId=$viagemId teveQuebra=$teveQuebra " +
                    "kgPerdido=$kgPerdido valorQuebra=$valorQuebra " +
                    "observacaoQuebra=${observacaoQuebra?.take(80)}",
            )
            try {
                when (
                    operations.finalizarViagem(
                        id = viagemId,
                        teveQuebra = teveQuebra,
                        kgPerdido = kgPerdido,
                        valorQuebra = valorQuebra,
                        observacaoQuebra = observacaoQuebra,
                    )
                ) {
                    OperationResult.Sent -> {
                        AppLog.d("FINALIZAR_VIAGEM", "sucesso para viagemId=$viagemId")
                        loadViagem(viagemId)
                        onSuccess(false)
                    }
                    OperationResult.Queued -> {
                        loadViagem(viagemId)
                        onSuccess(true)
                    }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(
                    "FINALIZAR_VIAGEM",
                    "HTTP ${e.code()} | body=${errorBody ?: e.message()}",
                )
                _actionError.value = userMessageForThrowable(e)
            } catch (e: Exception) {
                Log.e("FINALIZAR_VIAGEM", "exceção inesperada: $e", e)
                _actionError.value = userMessageForThrowable(e)
            } finally {
                _isFinalizando.value = false
            }
        }
    }
}
