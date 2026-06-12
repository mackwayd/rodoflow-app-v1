package com.example.rodoflow.ui.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rodoflow.data.local.entity.PendingOperationEntity
import com.example.rodoflow.ui.components.AppTopBar
import com.example.rodoflow.ui.components.LocalSnackbar
import com.example.rodoflow.ui.theme.AppButtonShape
import com.example.rodoflow.ui.theme.AppCardShape
import com.example.rodoflow.ui.util.MSG_SYNC_IN_PROGRESS

private val FailedColor = Color(0xFFB3261E)
private val PendingColor = Color(0xFF7A4F00)

@Composable
fun PendingSyncScreen(
    onNavigateBack: () -> Unit,
    viewModel: PendingSyncViewModel = viewModel(),
) {
    val context = LocalContext.current
    val showSnackbar = LocalSnackbar.current
    val items by viewModel.items.collectAsStateWithLifecycle()
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Envios pendentes",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (items.isNotEmpty()) {
                Button(
                    onClick = {
                        showSnackbar(MSG_SYNC_IN_PROGRESS)
                        viewModel.syncNow(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppButtonShape,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                ) {
                    Text("Enviar tudo agora")
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nenhum envio pendente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { it.id }) { operation ->
                        PendingOperationCard(
                            operation = operation,
                            onDelete = { confirmDeleteId = operation.id },
                        )
                    }
                }
            }
        }
    }

    confirmDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("Remover da fila?") },
            text = {
                Text(
                    "Este lançamento será apagado do celular e não será enviado ao servidor.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.discard(id)
                        confirmDeleteId = null
                        showSnackbar("Envio removido da fila")
                    },
                ) {
                    Text("Remover", color = FailedColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun PendingOperationCard(
    operation: PendingOperationEntity,
    onDelete: () -> Unit,
) {
    val statusColor = when (operation.status) {
        PendingOperationEntity.STATUS_FAILED -> FailedColor
        else -> PendingColor
    }
    val statusLabel = when (operation.status) {
        PendingOperationEntity.STATUS_FAILED -> "Erro no servidor"
        else -> "Aguardando internet"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardShape,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = humanizePendingOperationType(operation.type),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = pendingOperationSubtitle(operation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = formatPendingCreatedAt(operation.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    operation.lastError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = FailedColor,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (operation.comprovantePath != null) {
                        Text(
                            text = "Com foto anexada",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Remover da fila",
                    )
                }
            }
        }
    }
}
