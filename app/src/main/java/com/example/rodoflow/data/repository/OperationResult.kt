package com.example.rodoflow.data.repository

sealed class OperationResult {
    data object Sent : OperationResult()
    data object Queued : OperationResult()
}
