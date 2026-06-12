package com.example.rodoflow.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.rodoflow.data.api.RetrofitInstance
import com.example.rodoflow.data.local.OptimisticCacheUpdater
import java.io.File

@Composable
fun ComprovanteImage(
    relativeUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (relativeUrl.isNullOrBlank()) return

    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .okHttpClient(RetrofitInstance.okHttpClient)
            .build()
    }
    val modelData = remember(relativeUrl) {
        resolveComprovanteModel(relativeUrl)
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(modelData)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        contentScale = ContentScale.Crop,
        loading = {
            Text(
                text = "Carregando comprovante…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        error = {
            Text(
                text = "Comprovante indisponível",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

private fun resolveComprovanteModel(relativeUrl: String): Any {
    if (relativeUrl.startsWith(OptimisticCacheUpdater.LOCAL_COMPROVANTE_PREFIX)) {
        return File(relativeUrl.removePrefix(OptimisticCacheUpdater.LOCAL_COMPROVANTE_PREFIX))
    }
    val asFile = File(relativeUrl)
    if (asFile.isAbsolute && asFile.exists()) {
        return asFile
    }
    val base = com.example.rodoflow.BuildConfig.RODOFLOW_API_BASE_URL.trimEnd('/')
    val path = relativeUrl.trim().let { if (it.startsWith("/")) it else "/$it" }
    return "$base$path"
}
