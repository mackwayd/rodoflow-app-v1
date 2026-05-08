package com.example.rodoflow.ui.viagens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.repository.ViagemRepository
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
        toneladasFinais: Double? = null,
        observacaoQuebra: String? = null,
    ) {
        viewModelScope.launch {
            _isFinalizando.value = true
            _actionError.value = null
            try {
                repository.finalizarViagem(
                    id = viagemId,
                    teveQuebra = teveQuebra,
                    toneladasFinais = toneladasFinais,
                    observacaoQuebra = observacaoQuebra,
                )
                loadViagem(viagemId)
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string().orEmpty()
                Log.e("FINALIZAR_VIAGEM", body.ifBlank { e.message() })
                _actionError.value = "Não foi possível finalizar a viagem. Verifique os dados e tente novamente."
            } catch (e: Exception) {
                Log.e("FINALIZAR_VIAGEM", e.toString(), e)
                _actionError.value = "Erro inesperado ao finalizar a viagem. Tente novamente."
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
