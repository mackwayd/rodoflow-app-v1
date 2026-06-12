package com.example.rodoflow.ui.financeiro

import com.example.rodoflow.data.AppConfig
import com.example.rodoflow.data.model.Viagem
import com.example.rodoflow.data.model.toResumoViagens
import com.example.rodoflow.data.util.VisibleWindowFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Garante que o financeiro do app reflete apenas viagens visíveis (últimos 30 dias).
 */
class FinanceiroVisibleWindowTest {

    private val now = Instant.parse("2026-05-19T15:00:00Z")
    private val days = AppConfig.DAYS_VISIBLE_IN_APP

    @Test
    fun financeiroResumos_emptyWhenAllTripsOutsideWindow() {
        val oldTrips = listOf(
            viagem(id = "1", dataInicio = "2026-03-01T10:00:00Z", status = "PAGA", saldo = 1500.0),
            viagem(id = "2", dataInicio = "2026-02-01T10:00:00Z", status = "FINALIZADA", saldo = 800.0),
        )

        val visiveis = VisibleWindowFilter.filterViagens(oldTrips, days = days, now = now)
        val resumos = visiveis.toResumoViagens()

        assertTrue(resumos.isEmpty())
        assertEquals(0.0, resumos.sumOf { it.saldoEmpresaOrFallback }, 0.001)
        assertEquals(0.0, resumos.sumOf { it.totalDespesas }, 0.001)
    }

    @Test
    fun financeiroResumos_sumsOnlyVisibleTrips() {
        val trips = listOf(
            viagem(id = "recent", dataInicio = "2026-05-18T10:00:00Z", status = "FINALIZADA", saldo = 200.0),
            viagem(id = "old", dataInicio = "2026-03-01T10:00:00Z", status = "PAGA", saldo = 9000.0),
        )

        val visiveis = VisibleWindowFilter.filterViagens(trips, days = days, now = now)
        val resumos = visiveis.toResumoViagens()

        assertEquals(1, resumos.size)
        assertEquals(200.0, resumos.sumOf { it.saldoEmpresaOrFallback }, 0.001)
    }

    @Test
    fun financeiroResumos_keepsEmAndamentoOutsideWindow() {
        val trips = listOf(
            viagem(id = "active", dataInicio = "2026-01-01T10:00:00Z", status = "EM_ANDAMENTO", saldo = 300.0),
        )

        val visiveis = VisibleWindowFilter.filterViagens(trips, days = days, now = now)
        val resumos = visiveis.toResumoViagens()

        assertEquals(1, resumos.size)
        assertEquals(300.0, resumos.sumOf { it.saldoEmpresaOrFallback }, 0.001)
    }

    private fun viagem(
        id: String,
        dataInicio: String,
        status: String,
        saldo: Double,
    ) = Viagem(
        id = id,
        origem = "A",
        destino = "B",
        dataInicio = dataInicio,
        status = status,
        saldoEmpresa = saldo,
        valorBruto = saldo,
    )
}
