package com.example.rodoflow.data.local.entity

data class PendingOperationEntity(
    val id: String,
    val type: String,
    val payloadJson: String,
    val comprovantePath: String? = null,
    val status: String = STATUS_PENDING,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_FAILED = "FAILED"
    }
}
