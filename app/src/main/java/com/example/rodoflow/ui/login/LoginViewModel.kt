package com.example.rodoflow.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.model.UsuarioMe
import com.example.rodoflow.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(
        email: String,
        password: String,
        onSuccess: (UsuarioMe, String) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.login(email.trim(), password).fold(
                onSuccess = { (me, userId) -> onSuccess(me, userId) },
                onFailure = { _errorMessage.value = it.message ?: "Erro ao entrar" },
            )
            _isLoading.value = false
        }
    }
}
