package com.example.rodoflow.ui.viagens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.AppServices
import com.example.rodoflow.data.repository.OperationResult
import com.example.rodoflow.ui.util.userMessageForHttpException
import com.example.rodoflow.ui.util.userMessageForThrowable
import kotlinx.coroutines.launch
import retrofit2.HttpException

class NovaViagemViewModel(
    private val operations: com.example.rodoflow.data.repository.OutgoingOperationsRepository =
        AppServices.outgoingOperations,
) : ViewModel() {

    fun createViagem(
        origem: String,
        destino: String,
        numeroToneladas: Double,
        valorTonelada: Double,
        cliente: String,
        cnpjCliente: String,
        tipoCarga: String,
        kmInicial: Double,
        onSuccess: (queued: Boolean) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                when (
                    operations.createViagem(
                        origem = origem,
                        destino = destino,
                        numeroToneladas = numeroToneladas,
                        valorTonelada = valorTonelada,
                        cliente = cliente,
                        cnpjCliente = cnpjCliente,
                        tipoCarga = tipoCarga,
                        kmInicial = kmInicial,
                    )
                ) {
                    OperationResult.Sent -> onSuccess(false)
                    OperationResult.Queued -> onSuccess(true)
                }
            } catch (e: HttpException) {
                Log.e("CREATE_VIAGEM", e.response()?.errorBody()?.string() ?: e.message())
                onError(userMessageForHttpException(e))
            } catch (e: Exception) {
                Log.e("CREATE_VIAGEM", e.toString(), e)
                onError(userMessageForThrowable(e))
            }
        }
    }
}
