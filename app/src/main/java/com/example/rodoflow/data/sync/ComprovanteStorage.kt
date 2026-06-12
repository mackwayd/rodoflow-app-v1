package com.example.rodoflow.data.sync

import android.content.Context
import com.example.rodoflow.data.util.ComprovantePayload
import java.io.File
import java.util.UUID

class ComprovanteStorage(
    private val context: Context,
) {
    private val rootDir: File
        get() = File(context.filesDir, "pending_sync/comprovantes").apply { mkdirs() }

    fun save(payload: ComprovantePayload): String {
        val ext = payload.fileName.substringAfterLast('.', "jpg")
        val file = File(rootDir, "${UUID.randomUUID()}.$ext")
        file.writeBytes(payload.bytes)
        return file.absolutePath
    }

    fun load(absolutePath: String): ComprovantePayload? {
        val file = File(absolutePath)
        if (!file.exists() || !file.isFile) return null
        val mime = when (file.extension.lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            else -> "image/jpeg"
        }
        return ComprovantePayload(
            bytes = file.readBytes(),
            mimeType = mime,
            fileName = file.name,
        )
    }

    fun delete(absolutePath: String?) {
        if (absolutePath.isNullOrBlank()) return
        runCatching { File(absolutePath).delete() }
    }
}
