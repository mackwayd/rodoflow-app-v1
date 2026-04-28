package com.example.rodoflow.ui.financeiro

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

    private val _resumos = MutableStateFlow<List<ResumoViagem>>(emptyList())
    val resumos: StateFlow<List<ResumoViagem>> = _resumos.asStateFlow()

    fun loadResumo() {
        viewModelScope.launch {
            _resumos.value = repository.getResumo()
        }
    }
}
