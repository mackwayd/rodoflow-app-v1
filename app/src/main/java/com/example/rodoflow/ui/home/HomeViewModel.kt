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

    private val _saldo = MutableStateFlow<List<SaldoMotorista>>(emptyList())
    val saldo: StateFlow<List<SaldoMotorista>> = _saldo.asStateFlow()

    fun loadSaldo() {
        viewModelScope.launch {
            _saldo.value = repository.getSaldo()
        }
    }
}
