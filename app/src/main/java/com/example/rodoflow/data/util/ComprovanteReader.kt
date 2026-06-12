package com.example.rodoflow.data.util

import android.content.Context
import android.net.Uri

data class ComprovantePayload(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
)

object ComprovanteReader {
    /** Lê a imagem do URI e comprime para envio (JPEG, até ~4 MB). */
    fun read(context: Context, uri: Uri): ComprovantePayload? =
        ComprovanteImageCompressor.compress(context, uri)
}
