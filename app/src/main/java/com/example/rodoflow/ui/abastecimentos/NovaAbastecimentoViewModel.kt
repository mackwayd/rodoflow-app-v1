package com.example.rodoflow.ui.abastecimentos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class NovaAbastecimentoViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    fun createAbastecimento(
        litros: Double,
        valorTotal: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                repository.createAbastecimento(
                    caminhaoId = "cam-1",
                    litros = litros,
                    valorTotal = valorTotal,
                    data = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()),
                )
                onSuccess()
            } catch (e: HttpException) {
                Log.e(
                    "CREATE_ABASTECIMENTO",
                    e.response()?.errorBody()?.string() ?: e.message()
                )
                onError("Não foi possível criar o abastecimento. Verifique os campos e tente novamente.")
            } catch (e: Exception) {
                Log.e("CREATE_ABASTECIMENTO", e.toString(), e)
                onError("Erro inesperado ao criar abastecimento. Tente novamente.")
            }
        }
    }
}
