package com.example.rodoflow.ui.abastecimentos

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.rodoflow.AppLog
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.rodoflow.ui.util.userMessageForThrowable
import retrofit2.HttpException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private const val MOTORISTA_ID = "motorista-1"
private const val CAMINHAO_ID = "cam-1"

class NovaAbastecimentoViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    private val _viagemAtual = MutableStateFlow<Viagem?>(null)
    val viagemAtual: StateFlow<Viagem?> = _viagemAtual.asStateFlow()

    private val _carregandoViagemAtual = MutableStateFlow(false)
    val carregandoViagemAtual: StateFlow<Boolean> = _carregandoViagemAtual.asStateFlow()

    fun carregarViagemAtual() {
        viewModelScope.launch {
            _carregandoViagemAtual.value = true
            try {
                val viagens = repository.getViagens(MOTORISTA_ID)
                _viagemAtual.value = viagens
                    .filter { it.status == "EM_ANDAMENTO" }
                    .maxByOrNull { it.dataInicio }
            } catch (e: Exception) {
                Log.e("LOAD_VIAGEM_ATUAL_ABAST", e.toString(), e)
                _viagemAtual.value = null
            } finally {
                _carregandoViagemAtual.value = false
            }
        }
    }

    fun createAbastecimento(
        litros: Double,
        valorLitro: Double,
        viagemId: String?,
        onSuccess: (vinculado: Boolean) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            AppLog.d(
                "CREATE_ABASTECIMENTO",
                "iniciando criar litros=$litros valorLitro=$valorLitro viagemId=$viagemId " +
                    "caminhaoId=$CAMINHAO_ID",
            )
            try {
                repository.createAbastecimento(
                    caminhaoId = CAMINHAO_ID,
                    litros = litros,
                    valorLitro = valorLitro,
                    data = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()),
                    viagemId = viagemId,
                )
                AppLog.d("CREATE_ABASTECIMENTO", "sucesso vinculado=${viagemId != null}")
                onSuccess(viagemId != null)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(
                    "CREATE_ABASTECIMENTO",
                    "HTTP ${e.code()} | body=${errorBody ?: e.message()}",
                )
                onError(userMessageForThrowable(e))
            } catch (e: Exception) {
                Log.e("CREATE_ABASTECIMENTO", "exceção inesperada: $e", e)
                onError(userMessageForThrowable(e))
            }
        }
    }
}
