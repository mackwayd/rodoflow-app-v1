package com.example.rodoflow.data.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun textPart(value: String): RequestBody = value.toRequestBody("text/plain".toMediaType())

fun imagePart(bytes: ByteArray, mimeType: String, fileName: String): MultipartBody.Part {
    val body = bytes.toRequestBody(mimeType.toMediaType())
    return MultipartBody.Part.createFormData("comprovante", fileName, body)
}
