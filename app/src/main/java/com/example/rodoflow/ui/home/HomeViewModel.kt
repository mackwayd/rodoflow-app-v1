package com.example.rodoflow.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.rodoflow.AppLog
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.SaldoMotorista
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.repository.FinanceiroRepository
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val MOTORISTA_ID = "motorista-1"

data class StatusCounts(
    val emAndamento: Int = 0,
    val finalizadas: Int = 0,
    val pagas: Int = 0,
)

class HomeViewModel(
    private val financeiroRepository: FinanceiroRepository = FinanceiroRepository(),
    private val viagemRepository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadFailed = MutableStateFlow(false)
    val loadFailed: StateFlow<Boolean> = _loadFailed.asStateFlow()

    private val _saldo = MutableStateFlow<List<SaldoMotorista>>(emptyList())
    val saldo: StateFlow<List<SaldoMotorista>> = _saldo.asStateFlow()

    private val _viagens = MutableStateFlow<List<Viagem>>(emptyList())
    val viagens: StateFlow<List<Viagem>> = _viagens.asStateFlow()

    /**
     * Viagem EM_ANDAMENTO mais recente, com payload detalhado vindo de getViagemById.
     * É necessário usar o detalhe (não o item da lista) porque o endpoint de listagem
     * não retorna [Viagem.totalDespesas] / [Viagem.totalAbastecimentos] populados —
     * a Home precisa desses totais para o card operacional da viagem atual.
     */
    private val _viagemAtual = MutableStateFlow<Viagem?>(null)
    val viagemAtual: StateFlow<Viagem?> = _viagemAtual.asStateFlow()

    val statusCounts: StateFlow<StatusCounts> = viagens
        .map { list ->
            StatusCounts(
                emAndamento = list.count { it.status == "EM_ANDAMENTO" },
                finalizadas = list.count { it.status == "FINALIZADA" },
                pagas = list.count { it.status == "PAGA" },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatusCounts(),
        )

    fun loadDashboard() {
        viewModelScope.launch {
            AppLog.d("HOME_RELOAD", "loadDashboard() iniciado")
            _loading.value = true
            _loadFailed.value = false
            try {
                val (saldoResult, viagensResult) = coroutineScope {
                    val saldoDeferred = async { financeiroRepository.getSaldo() }
                    val viagensDeferred = async { viagemRepository.getViagens(MOTORISTA_ID) }
                    saldoDeferred.await() to viagensDeferred.await()
                }

                AppLog.d(
                    "HOME_DATA",
                    "saldo recebido size=${saldoResult.size} primeiro=${saldoResult.firstOrNull()}",
                )
                AppLog.d(
                    "HOME_DATA",
                    "viagens recebidas size=${viagensResult.size} statuses=" +
                        viagensResult.groupingBy { it.status }.eachCount(),
                )

                _saldo.value = saldoResult
                _viagens.value = viagensResult

                val viagemEmAndamentoLista = viagensResult
                    .filter { it.status == "EM_ANDAMENTO" }
                    .maxByOrNull { it.dataInicio }

                if (viagemEmAndamentoLista == null) {
                    AppLog.d("HOME_DATA", "nenhuma viagem EM_ANDAMENTO encontrada")
                    _viagemAtual.value = null
                } else {
                    AppLog.d(
                        "HOME_DATA",
                        "viagem EM_ANDAMENTO (lista) id=${viagemEmAndamentoLista.id} " +
                            "totalDespesas=${viagemEmAndamentoLista.totalDespesas} " +
                            "totalAbastecimentos=${viagemEmAndamentoLista.totalAbastecimentos} " +
                            "(buscando detalhe para totais reais)",
                    )
                    val detalhe = runCatching { viagemRepository.getViagemById(viagemEmAndamentoLista.id) }
                        .onFailure { Log.e("HOME_DATA", "falha ao carregar detalhe da viagem atual: $it", it) }
                        .getOrNull()
                    val viagemFinal = detalhe ?: viagemEmAndamentoLista
                    AppLog.d(
                        "HOME_DATA",
                        "viagemAtual aplicada id=${viagemFinal.id} " +
                            "valorBruto=${viagemFinal.valorBruto} " +
                            "totalDespesas=${viagemFinal.totalDespesas} " +
                            "totalAbastecimentos=${viagemFinal.totalAbastecimentos} " +
                            "saldoEmpresa=${viagemFinal.saldoEmpresa} " +
                            "fonte=${if (detalhe != null) "detalhe" else "lista-fallback"}",
                    )
                    _viagemAtual.value = viagemFinal
                }

                AppLog.d("HOME_RELOAD", "loadDashboard() concluído com sucesso")
            } catch (e: Exception) {
                Log.e("HOME_RELOAD", "loadDashboard() falhou: $e", e)
                _loadFailed.value = true
            } finally {
                _loading.value = false
            }
        }
    }
}
