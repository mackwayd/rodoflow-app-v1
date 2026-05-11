package com.example.rodoflow.ui.components

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Callback global para enfileirar mensagens em um único [androidx.compose.material3.SnackbarHost]
 * hospedado no [androidx.compose.material3.Scaffold] raiz do app.
 *
 * Uso típico:
 * ```
 * val showSnackbar = LocalSnackbar.current
 * showSnackbar("Despesa registrada")
 * ```
 *
 * O default é um no-op para que previews/testes sem provider não quebrem.
 */
val LocalSnackbar = staticCompositionLocalOf<(String) -> Unit> { { } }
