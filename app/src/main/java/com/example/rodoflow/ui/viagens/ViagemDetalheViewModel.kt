package com.example.rodoflow.ui.viagens

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.rodoflow.AppLog
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.repository.ViagemRepository
import com.example.rodoflow.ui.util.userMessageForThrowable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ViagemDetalheViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    private val _viagem = MutableStateFlow<Viagem?>(null)
    val viagem: StateFlow<Viagem?> = _viagem.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _notFound = MutableStateFlow(false)
    val notFound: StateFlow<Boolean> = _notFound.asStateFlow()
    private val _isFinalizando = MutableStateFlow(false)
    val isFinalizando: StateFlow<Boolean> = _isFinalizando.asStateFlow()
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    fun loadViagem(viagemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _notFound.value = false
            _viagem.value = repository.getViagemById(viagemId)
            if (_viagem.value == null) {
                _notFound.value = true
            }
            _isLoading.value = false
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
        onSuccess: () -> Unit = {},
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
                repository.finalizarViagem(
                    id = viagemId,
                    teveQuebra = teveQuebra,
                    kgPerdido = kgPerdido,
                    valorQuebra = valorQuebra,
                    observacaoQuebra = observacaoQuebra,
                )
                AppLog.d("FINALIZAR_VIAGEM", "sucesso para viagemId=$viagemId")
                loadViagem(viagemId)
                onSuccess()
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

    fun marcarComoPago(viagemId: String) {
        viewModelScope.launch {
            runCatching {
                repository.pagarViagem(viagemId)
            }.onSuccess {
                loadViagem(viagemId)
            }
        }
    }
}
