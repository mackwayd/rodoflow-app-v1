package com.example.rodoflow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.SaldoMotorista
import com.example.rodoflow.data.repository.FinanceiroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FinanceiroRepository = FinanceiroRepository(),
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saldo = MutableStateFlow<List<SaldoMotorista>>(emptyList())
    val saldo: StateFlow<List<SaldoMotorista>> = _saldo.asStateFlow()

    fun loadSaldo() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _saldo.value = repository.getSaldo()
            } catch (e: Exception) {
                _error.value = e.message ?: "Não foi possível carregar o saldo."
            } finally {
                _loading.value = false
            }
        }
    }
}
