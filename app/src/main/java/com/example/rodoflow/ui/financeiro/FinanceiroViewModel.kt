package com.example.rodoflow.ui.financeiro

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.AppServices
import com.example.rodoflow.DriverContext
import com.example.rodoflow.data.model.ResumoViagem
import com.example.rodoflow.data.model.toResumoViagens
import com.example.rodoflow.data.repository.ViagemListLoadResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Carrega resumos financeiros apenas das viagens visíveis no app (janela de
 * [com.example.rodoflow.data.AppConfig.DAYS_VISIBLE_IN_APP] dias via [ViagemRepository.loadViagens]).
 * Viagens fora da janela somem do financeiro junto com as listas — sem alterar o backend.
 */
class FinanceiroViewModel(
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

    private val _resumos = MutableStateFlow<List<ResumoViagem>>(emptyList())
    val resumos: StateFlow<List<ResumoViagem>> = _resumos.asStateFlow()

    fun loadResumo() {
        viewModelScope.launch {
            val hadData = _resumos.value.isNotEmpty()
            _loading.value = true
            _loadFailed.value = false
            _refreshFailedWithData.value = false
            when (val result = viagemRepository.loadViagens(DriverContext.motoristaId)) {
                is ViagemListLoadResult.Success -> {
                    _isShowingCachedData.value = result.fromCache
                    _resumos.value = result.viagens.toResumoViagens()
                }
                is ViagemListLoadResult.Error -> {
                    Log.e("FINANCEIRO_LOAD", "loadResumo falhou: ${result.cause}", result.cause)
                    _refreshFailedWithData.value = hadData
                    _loadFailed.value = _resumos.value.isEmpty()
                }
            }
            _loading.value = false
        }
    }
}
