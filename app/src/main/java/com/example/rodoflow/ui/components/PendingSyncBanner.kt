package com.example.rodoflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rodoflow.ui.theme.AppBannerShape

private val BannerSurface = Color(0xFFFFF3E0)
private val BannerOnSurface = Color(0xFF7A4F00)

@Composable
fun PendingSyncBanner(
    pendingCount: Int,
    failedCount: Int,
    onViewDetails: () -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (pendingCount + failedCount <= 0) return

    val title = when {
        pendingCount > 0 && failedCount > 0 ->
            "$pendingCount envio(s) pendente(s) · $failedCount com erro"
        pendingCount > 0 ->
            if (pendingCount == 1) {
                "1 envio aguardando internet"
            } else {
                "$pendingCount envios aguardando internet"
            }
        else ->
            if (failedCount == 1) {
                "1 envio não foi aceito pelo servidor"
            } else {
                "$failedCount envios não foram aceitos pelo servidor"
            }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BannerSurface, AppBannerShape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudUpload,
            contentDescription = null,
            tint = BannerOnSurface,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onViewDetails),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = BannerOnSurface,
            )
            Text(
                text = "Toque para ver detalhes · envio automático com internet",
                style = MaterialTheme.typography.bodySmall,
                color = BannerOnSurface.copy(alpha = 0.9f),
            )
        }
        TextButton(onClick = onViewDetails) {
            Text("Ver", color = BannerOnSurface)
        }
        TextButton(onClick = onSyncNow) {
            Text("Enviar", color = BannerOnSurface)
        }
    }
}
