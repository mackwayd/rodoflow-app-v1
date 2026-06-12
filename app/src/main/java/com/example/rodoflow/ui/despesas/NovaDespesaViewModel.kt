package com.example.rodoflow.ui.despesas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.util.ComprovantePayload
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.AppServices
import com.example.rodoflow.data.repository.OperationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.rodoflow.ui.util.userMessageForThrowable
import retrofit2.HttpException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import com.example.rodoflow.DriverContext

class NovaDespesaViewModel(
    private val operations: com.example.rodoflow.data.repository.OutgoingOperationsRepository =
        AppServices.outgoingOperations,
    private val readRepository: com.example.rodoflow.data.repository.ViagemRepository =
        AppServices.viagemRepository,
) : ViewModel() {

    private val _viagemAtual = MutableStateFlow<Viagem?>(null)
    val viagemAtual: StateFlow<Viagem?> = _viagemAtual.asStateFlow()

    private val _carregandoViagemAtual = MutableStateFlow(false)
    val carregandoViagemAtual: StateFlow<Boolean> = _carregandoViagemAtual.asStateFlow()

    fun carregarViagemAtual() {
        viewModelScope.launch {
            _carregandoViagemAtual.value = true
            try {
                val viagens = readRepository.getViagens(DriverContext.motoristaId)
                _viagemAtual.value = viagens
                    .filter { it.status == "EM_ANDAMENTO" }
                    .maxByOrNull { it.dataInicio }
            } catch (e: Exception) {
                Log.e("LOAD_VIAGEM_ATUAL_DESPESA", e.toString(), e)
                _viagemAtual.value = null
            } finally {
                _carregandoViagemAtual.value = false
            }
        }
    }

    fun createDespesa(
        descricao: String,
        valor: Double,
        tipo: String,
        viagemId: String?,
        comprovante: ComprovantePayload?,
        onSuccess: (vinculada: Boolean, queued: Boolean) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (
                    operations.createDespesa(
                        caminhaoId = DriverContext.CAMINHAO_ID,
                        valor = valor,
                        tipo = tipo,
                        data = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()),
                        descricao = descricao.ifBlank { null },
                        viagemId = viagemId,
                        comprovante = comprovante,
                    )
                ) {
                    OperationResult.Sent -> onSuccess(viagemId != null, false)
                    OperationResult.Queued -> onSuccess(viagemId != null, true)
                }
            } catch (e: HttpException) {
                Log.e(
                    "CREATE_DESPESA",
                    e.response()?.errorBody()?.string() ?: e.message()
                )
                onError(userMessageForThrowable(e))
            } catch (e: Exception) {
                Log.e("CREATE_DESPESA", e.toString(), e)
                onError(userMessageForThrowable(e))
            }
        }
    }
}
