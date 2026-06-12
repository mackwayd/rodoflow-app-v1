package com.example.rodoflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.AppServices
import com.example.rodoflow.data.repository.AuthLoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel : ViewModel() {
    private val authRepository = AppServices.authRepository
    private val authSession = AppServices.authSession

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onSenhaChange(value: String) {
        _uiState.update { it.copy(senha = value, errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val senha = _uiState.value.senha
        if (email.isEmpty() || senha.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Informe e-mail e senha.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.login(email, senha)) {
                is AuthLoginResult.Success -> {
                    authSession.save(result.session)
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is AuthLoginResult.InvalidCredentials -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is AuthLoginResult.WrongRole -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is AuthLoginResult.NetworkError -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is AuthLoginResult.UnknownError -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
