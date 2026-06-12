package com.example.rodoflow.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.rodoflow.AppLog
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.AppServices
import com.example.rodoflow.DriverContext
import com.example.rodoflow.data.model.SaldoMotorista
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.repository.ViagemDetailLoadResult
import com.example.rodoflow.data.repository.ViagemListLoadResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatusCounts(
    val emAndamento: Int = 0,
    val finalizadas: Int = 0,
    val pagas: Int = 0,
)

class HomeViewModel(
    private val viagemRepository: com.example.rodoflow.data.repository.ViagemRepository =
        AppServices.viagemRepository,
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadFailed = MutableStateFlow(false)
    val loadFailed: StateFlow<Boolean> = _loadFailed.asStateFlow()

    private val _isShowingCachedData = MutableStateFlow(false)
    val isShowingCachedData: StateFlow<Boolean> = _isShowingCachedData.asStateFlow()

    private val _refreshFailedWithData = MutableStateFlow(false)
    val refreshFailedWithData: StateFlow<Boolean> = _refreshFailedWithData.asStateFlow()

    private val _saldo = MutableStateFlow<List<SaldoMotorista>>(emptyList())
    val saldo: StateFlow<List<SaldoMotorista>> = _saldo.asStateFlow()

    private val _viagens = MutableStateFlow<List<Viagem>>(emptyList())
    val viagens: StateFlow<List<Viagem>> = _viagens.asStateFlow()

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
            val hadData = _viagens.value.isNotEmpty()
            _loading.value = true
            _loadFailed.value = false
            _refreshFailedWithData.value = false
            try {
                when (val result = viagemRepository.loadViagens(DriverContext.motoristaId)) {
                    is ViagemListLoadResult.Success -> {
                        _isShowingCachedData.value = result.fromCache
                        _saldo.value = saldoFromViagens(result.viagens)
                        _viagens.value = result.viagens
                        loadViagemAtual(result.viagens)
                        AppLog.d("HOME_RELOAD", "loadDashboard() concluído com sucesso")
                    }
                    is ViagemListLoadResult.Error -> {
                        Log.e("HOME_RELOAD", "loadDashboard() falhou: ${result.cause}", result.cause)
                        _refreshFailedWithData.value = hadData
                        _loadFailed.value = _viagens.value.isEmpty()
                    }
                }
            } catch (e: Exception) {
                Log.e("HOME_RELOAD", "loadDashboard() falhou: $e", e)
                _refreshFailedWithData.value = hadData
                _loadFailed.value = _viagens.value.isEmpty()
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun loadViagemAtual(viagensResult: List<Viagem>) {
        val viagemEmAndamentoLista = viagensResult
            .filter { it.status == "EM_ANDAMENTO" }
            .maxByOrNull { it.dataInicio }

        if (viagemEmAndamentoLista == null) {
            _viagemAtual.value = null
            return
        }

        when (val detalhe = viagemRepository.loadViagemById(viagemEmAndamentoLista.id)) {
            is ViagemDetailLoadResult.Success -> {
                _viagemAtual.value = detalhe.viagem
            }
            else -> {
                _viagemAtual.value = viagemEmAndamentoLista
            }
        }
    }

    private fun saldoFromViagens(viagens: List<Viagem>): List<SaldoMotorista> {
        val total = viagens.sumOf { it.saldoEmpresa }
        return listOf(
            SaldoMotorista(
                motoristaId = DriverContext.motoristaId,
                motoristaNome = "Motorista",
                totalViagens = viagens.size,
                saldoPendente = total,
            ),
        )
    }
}
