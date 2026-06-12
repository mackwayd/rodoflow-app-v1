package com.example.rodoflow.data.util

import com.example.rodoflow.data.AppConfig
import com.example.rodoflow.data.model.AbastecimentoViagem
import com.example.rodoflow.data.model.DespesaViagem
import com.example.rodoflow.data.model.Viagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class VisibleWindowFilterTest {

    private val zone = ZoneId.of("America/Sao_Paulo")
    private val now = Instant.parse("2026-05-19T15:00:00Z")
    private val days = AppConfig.DAYS_VISIBLE_IN_APP

    @Test
    fun isWithinVisibleWindow_includesRecordFromCutoffDay() {
        assertTrue(
            VisibleWindowFilter.isWithinVisibleWindow(
                isoDate = "2026-04-19T10:00:00Z",
                days = days,
                zone = zone,
                now = now,
            ),
        )
    }

    @Test
    fun isWithinVisibleWindow_excludesOlderRecord() {
        assertFalse(
            VisibleWindowFilter.isWithinVisibleWindow(
                isoDate = "2026-04-18T23:59:59Z",
                days = days,
                zone = zone,
                now = now,
            ),
        )
    }

    @Test
    fun filterViagens_keepsRecentAndHidesOldFinalized() {
        val recent = viagem(id = "1", dataInicio = "2026-05-18T10:00:00Z", status = "FINALIZADA")
        val old = viagem(id = "2", dataInicio = "2026-04-01T10:00:00Z", status = "PAGA")

        val result = VisibleWindowFilter.filterViagens(listOf(recent, old), days = days, now = now)

        assertEquals(1, result.size)
        assertEquals("1", result.first().id)
    }

    @Test
    fun filterViagens_alwaysKeepsEmAndamentoEvenWhenOld() {
        val oldActive = viagem(
            id = "active",
            dataInicio = "2026-03-01T10:00:00Z",
            status = "EM_ANDAMENTO",
        )

        val result = VisibleWindowFilter.filterViagens(listOf(oldActive), days = days, now = now)

        assertEquals(1, result.size)
        assertEquals("active", result.first().id)
    }

    @Test
    fun applyToViagem_filtersNestedDespesasAndAbastecimentos() {
        val viagem = viagem(
            id = "v1",
            dataInicio = "2026-05-18T10:00:00Z",
            status = "FINALIZADA",
        ).copy(
            despesas = listOf(
                DespesaViagem(id = "d1", valor = 10.0, data = "2026-05-18T10:00:00Z"),
                DespesaViagem(id = "d2", valor = 20.0, data = "2026-03-01T10:00:00Z"),
            ),
            abastecimentos = listOf(
                AbastecimentoViagem(id = "a1", valorTotal = 30.0, data = "2026-05-17T10:00:00Z"),
                AbastecimentoViagem(id = "a2", valorTotal = 40.0, data = "2026-03-01T10:00:00Z"),
            ),
            totalDespesas = 30.0,
            totalAbastecimentos = 70.0,
        )

        val filtered = VisibleWindowFilter.applyToViagem(viagem, days = days, now = now)

        assertNotNull(filtered)
        assertEquals(1, filtered!!.despesas.size)
        assertEquals("d1", filtered.despesas.first().id)
        assertEquals(10.0, filtered.totalDespesas, 0.001)
        assertEquals(1, filtered.abastecimentos.size)
        assertEquals(30.0, filtered.totalAbastecimentos, 0.001)
    }

    @Test
    fun applyToViagem_returnsNullForOldFinalizedTrip() {
        val old = viagem(id = "old", dataInicio = "2026-03-01T10:00:00Z", status = "PAGA")

        assertNull(VisibleWindowFilter.applyToViagem(old, days = days, now = now))
    }

    private fun viagem(id: String, dataInicio: String, status: String) = Viagem(
        id = id,
        origem = "A",
        destino = "B",
        dataInicio = dataInicio,
        status = status,
    )
}
