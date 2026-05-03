package com.example.rodoflow.ui.session

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.data.model.UsuarioMe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSessionViewModel : ViewModel() {

    private val _authUserId = MutableStateFlow<String?>(null)
    val authUserId: StateFlow<String?> = _authUserId.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    fun selectProfile(profile: UserProfile) {
        _profile.value = profile
    }

    fun onAuthenticated(me: UsuarioMe, authUserId: String) {
        _authUserId.value = authUserId
        _profile.value = when (me.tipo.uppercase()) {
            "ADMIN" -> UserProfile.Admin
            "MOTORISTA" -> UserProfile.Motorista
            else -> UserProfile.Motorista
        }
    }
}

@Composable
fun rememberAppSessionViewModel(): AppSessionViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(viewModelStoreOwner = activity)
}
