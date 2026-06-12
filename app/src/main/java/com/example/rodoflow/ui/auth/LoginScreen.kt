package com.example.rodoflow.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.R
import com.example.rodoflow.ui.theme.AppButtonShape
import com.example.rodoflow.ui.theme.AppCardShape
import com.example.rodoflow.ui.theme.RodoFlowBlue
import com.example.rodoflow.ui.theme.RodoFlowNavyElevated

private val FieldShape = RoundedCornerShape(12.dp)

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var senhaVisivel by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to RodoFlowBlue.copy(alpha = 0.22f),
                            0.35f to MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                            1f to MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 10.dp,
                color = RodoFlowNavyElevated,
                border = BorderStroke(
                    width = 1.dp,
                    color = RodoFlowBlue.copy(alpha = 0.35f),
                ),
            ) {
                Image(
                    painter = painterResource(R.drawable.rodoflow_logo),
                    contentDescription = "RodoFlow",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Bem-vindo de volta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Entre com seu acesso de motorista para\ncontinuar suas viagens.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppCardShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                ),
                tonalElevation = 1.dp,
                shadowElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (uiState.errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                            ),
                        ) {
                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("E-mail") },
                        placeholder = { Text("seu@email.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        singleLine = true,
                        shape = FieldShape,
                        colors = fieldColors(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        enabled = !uiState.isLoading,
                    )

                    OutlinedTextField(
                        value = uiState.senha,
                        onValueChange = viewModel::onSenhaChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Senha") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (senhaVisivel) {
                                        "Ocultar senha"
                                    } else {
                                        "Mostrar senha"
                                    },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        singleLine = true,
                        shape = FieldShape,
                        colors = fieldColors(),
                        visualTransformation = if (senhaVisivel) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.login(onLoggedIn) },
                        ),
                        enabled = !uiState.isLoading,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.login(onLoggedIn) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = AppButtonShape,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 6.dp,
                        ),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Entrando…")
                        } else {
                            Text(
                                text = "Entrar",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Esqueceu a senha? Peça ao administrador\npara redefinir seu acesso no painel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RodoFlowBlue.copy(alpha = 0.75f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
    focusedLabelColor = RodoFlowBlue,
    cursorColor = RodoFlowBlue,
    focusedLeadingIconColor = RodoFlowBlue,
)
