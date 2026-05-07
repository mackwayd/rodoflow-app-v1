package com.example.rodoflow.ui.despesas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rodoflow.data.repository.ViagemRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class NovaDespesaViewModel(
    private val repository: ViagemRepository = ViagemRepository(),
) : ViewModel() {

    fun createDespesa(
        descricao: String,
        valor: Double,
        tipo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                repository.createDespesa(
                    caminhaoId = "cam-1",
                    valor = valor,
                    tipo = tipo,
                    data = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()),
                    descricao = descricao.ifBlank { null },
                )
                onSuccess()
            } catch (e: HttpException) {
                Log.e(
                    "CREATE_DESPESA",
                    e.response()?.errorBody()?.string() ?: e.message()
                )
                onError("Não foi possível criar a despesa. Verifique os campos e tente novamente.")
            } catch (e: Exception) {
                Log.e("CREATE_DESPESA", e.toString(), e)
                onError("Erro inesperado ao criar despesa. Tente novamente.")
            }
        }
    }
}
