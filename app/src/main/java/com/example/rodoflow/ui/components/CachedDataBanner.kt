package com.example.rodoflow.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rodoflow.ui.theme.AppBannerShape
import com.example.rodoflow.ui.util.MSG_CACHED_DATA_BANNER
import com.example.rodoflow.ui.util.MSG_REFRESH_FAILED_WITH_CACHE

private val CachedSurface = Color(0xFFE3F2FD)
private val CachedOnSurface = Color(0xFF1565C0)
private val WarningSurface = Color(0xFFFFF3E0)
private val WarningOnSurface = Color(0xFFB45309)

@Composable
fun CachedDataBanner(
    isShowingCachedData: Boolean,
    refreshFailedWithData: Boolean,
    modifier: Modifier = Modifier,
) {
    val message = when {
        refreshFailedWithData -> MSG_REFRESH_FAILED_WITH_CACHE
        isShowingCachedData -> MSG_CACHED_DATA_BANNER
        else -> return
    }
    val (bg, fg) = if (refreshFailedWithData) {
        WarningSurface to WarningOnSurface
    } else {
        CachedSurface to CachedOnSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = AppBannerShape,
        color = bg,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = fg,
        )
    }
}
