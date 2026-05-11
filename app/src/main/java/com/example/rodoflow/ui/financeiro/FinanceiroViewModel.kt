package com.example.rodoflow.ui.financeiro

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.ResumoViagem
import com.example.rodoflow.data.repository.FinanceiroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinanceiroViewModel(
    private val repository: FinanceiroRepository = FinanceiroRepository(),
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadFailed = MutableStateFlow(false)
    val loadFailed: StateFlow<Boolean> = _loadFailed.asStateFlow()

    private val _resumos = MutableStateFlow<List<ResumoViagem>>(emptyList())
    val resumos: StateFlow<List<ResumoViagem>> = _resumos.asStateFlow()

    fun loadResumo() {
        viewModelScope.launch {
            _loading.value = true
            _loadFailed.value = false
            try {
                _resumos.value = repository.getResumo()
            } catch (e: Exception) {
                Log.e("FINANCEIRO_LOAD", "loadResumo falhou: $e", e)
                _loadFailed.value = true
            } finally {
                _loading.value = false
            }
        }
    }
}
