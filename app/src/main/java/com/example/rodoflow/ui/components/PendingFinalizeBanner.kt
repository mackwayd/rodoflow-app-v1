package com.example.rodoflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rodoflow.ui.theme.AppBannerShape

private val BannerSurface = Color(0xFFE3F2FD)
private val BannerOnSurface = Color(0xFF0D47A1)

@Composable
fun PendingFinalizeBanner(modifier: Modifier = Modifier) {
    Text(
        text = "Finalização salva no celular. Será enviada quando houver internet.",
        modifier = modifier
            .fillMaxWidth()
            .background(BannerSurface, AppBannerShape)
            .padding(12.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = BannerOnSurface,
    )
}
