package com.example.rodoflow.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ComprovanteImageCompressor {
    private const val MAX_DIMENSION = 1920
    private const val MAX_BYTES = 4 * 1024 * 1024
    private const val INITIAL_QUALITY = 82
    private const val MIN_QUALITY = 55

    fun compress(context: Context, uri: Uri): ComprovantePayload? {
        val decoded = decodeScaledBitmap(context, uri) ?: return null
        val bitmap = applyExifOrientation(context, uri, decoded)
        if (bitmap !== decoded) decoded.recycle()

        return try {
            var quality = INITIAL_QUALITY
            var bytes: ByteArray
            do {
                bytes = jpegBytes(bitmap, quality)
                quality -= 8
            } while (bytes.size > MAX_BYTES && quality >= MIN_QUALITY)

            if (bytes.isEmpty()) return null
            ComprovantePayload(
                bytes = bytes,
                mimeType = "image/jpeg",
                fileName = "comprovante.jpg",
            )
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeScaledBitmap(context: Context, uri: Uri): Bitmap? {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, boundsOpts)
        } ?: return null

        val sampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, MAX_DIMENSION)
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var inSampleSize = 1
        if (height > maxDim || width > maxDim) {
            var halfH = height / 2
            var halfW = width / 2
            while (halfH / inSampleSize >= maxDim && halfW / inSampleSize >= maxDim) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun applyExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val rotation = context.contentResolver.openInputStream(uri)?.use { input ->
            val exif = ExifInterface(input)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        if (rotation == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(rotation) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true) ?: bitmap
    }

    private fun jpegBytes(source: Bitmap, quality: Int): ByteArray {
        var working = source
        val longest = max(working.width, working.height)
        if (longest > MAX_DIMENSION) {
            val scale = MAX_DIMENSION.toFloat() / longest
            val w = (working.width * scale).roundToInt().coerceAtLeast(1)
            val h = (working.height * scale).roundToInt().coerceAtLeast(1)
            working = Bitmap.createScaledBitmap(working, w, h, true)
        }
        return ByteArrayOutputStream().use { stream ->
            working.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }.also {
            if (working !== source) working.recycle()
        }
    }
}
