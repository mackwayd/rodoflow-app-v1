package com.example.rodoflow.ui.viagens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.AppServices
import com.example.rodoflow.DriverContext
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.repository.ViagemListLoadResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViagensViewModel(
    private val repository: com.example.rodoflow.data.repository.ViagemRepository =
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

    private val _viagens = MutableStateFlow<List<Viagem>>(emptyList())
    val viagens: StateFlow<List<Viagem>> = _viagens.asStateFlow()

    val hasViagemEmAndamento: Boolean
        get() = _viagens.value.any { it.status == "EM_ANDAMENTO" }

    fun loadViagens() {
        viewModelScope.launch {
            val hadData = _viagens.value.isNotEmpty()
            _loading.value = true
            _loadFailed.value = false
            _refreshFailedWithData.value = false
            when (val result = repository.loadViagens(DriverContext.motoristaId)) {
                is ViagemListLoadResult.Success -> {
                    _isShowingCachedData.value = result.fromCache
                    _viagens.value = result.viagens
                }
                is ViagemListLoadResult.Error -> {
                    Log.e("VIAGENS_LOAD", "loadViagens falhou: ${result.cause}", result.cause)
                    _refreshFailedWithData.value = hadData
                    _loadFailed.value = _viagens.value.isEmpty()
                }
            }
            _loading.value = false
        }
    }
}
