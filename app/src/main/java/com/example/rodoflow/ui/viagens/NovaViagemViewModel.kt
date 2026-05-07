package com.example.rodoflow.ui.viagens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class NovaViagemViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    fun createViagem(
        origem: String,
        destino: String,
        valorBruto: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                repository.createViagem(
                    motoristaId = "motorista-1",
                    caminhaoId = "cam-1",
                    origem = origem,
                    destino = destino,
                    valorBruto = valorBruto,
                    dataInicio = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()),
                )
                onSuccess()
            } catch (e: HttpException) {
                Log.e("CREATE_VIAGEM", e.response()?.errorBody()?.string() ?: e.message())
                onError("Não foi possível criar a viagem. Verifique os campos e tente novamente.")
            } catch (e: Exception) {
                Log.e("CREATE_VIAGEM", e.toString(), e)
                onError("Erro inesperado ao criar viagem. Tente novamente.")
            }
        }
    }
}
