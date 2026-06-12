package com.example.rodoflow.data.util

import com.example.rodoflow.data.AppConfig
import com.example.rodoflow.data.model.AbastecimentoViagem
import com.example.rodoflow.data.model.DespesaViagem
import com.example.rodoflow.data.model.Viagem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Filtro de janela visível no app do motorista.
 * Os registros permanecem no servidor; apenas deixam de ser exibidos após o período configurado.
 */
object VisibleWindowFilter {

    private val appZoneId: ZoneId = ZoneId.of("America/Sao_Paulo")

    fun visibleWindowStart(
        days: Int = AppConfig.DAYS_VISIBLE_IN_APP,
        zone: ZoneId = appZoneId,
        now: Instant = Instant.now(),
    ): Instant {
        val today = LocalDate.ofInstant(now, zone)
        return today.minusDays(days.toLong()).atStartOfDay(zone).toInstant()
    }

    fun isWithinVisibleWindow(
        isoDate: String?,
        days: Int = AppConfig.DAYS_VISIBLE_IN_APP,
        zone: ZoneId = appZoneId,
        now: Instant = Instant.now(),
    ): Boolean {
        val instant = parseIsoToInstant(isoDate) ?: return false
        return !instant.isBefore(visibleWindowStart(days, zone, now))
    }

    fun filterViagens(
        viagens: List<Viagem>,
        days: Int = AppConfig.DAYS_VISIBLE_IN_APP,
        now: Instant = Instant.now(),
    ): List<Viagem> {
        return viagens.mapNotNull { applyToViagem(it, days, now) }
    }

    fun applyToViagem(
        viagem: Viagem,
        days: Int = AppConfig.DAYS_VISIBLE_IN_APP,
        now: Instant = Instant.now(),
    ): Viagem? {
        if (!isViagemVisible(viagem, days, now)) return null
        return viagem.withFilteredNestedEntries(days, now)
    }

    private fun isViagemVisible(
        viagem: Viagem,
        days: Int,
        now: Instant,
    ): Boolean {
        // Viagens em andamento permanecem visíveis independentemente da data de início.
        if (viagem.status == "EM_ANDAMENTO") return true
        return isWithinVisibleWindow(viagem.dataInicio, days, appZoneId, now)
    }

    private fun Viagem.withFilteredNestedEntries(days: Int, now: Instant): Viagem {
        val despesasVisiveis = despesas.filterVisibleDespesas(days, now)
        val abastecimentosVisiveis = abastecimentos.filterVisibleAbastecimentos(days, now)
        return copy(
            despesas = despesasVisiveis,
            abastecimentos = abastecimentosVisiveis,
            totalDespesas = despesasVisiveis.sumOf { it.valor },
            totalAbastecimentos = abastecimentosVisiveis.sumOf { it.valorTotal },
        )
    }

    fun List<DespesaViagem>.filterVisibleDespesas(
        days: Int = AppConfig.DAYS_VISIBLE_IN_APP,
        now: Instant = Instant.now(),
    ): List<DespesaViagem> = filter { isWithinVisibleWindow(it.data, days, appZoneId, now) }

    fun List<AbastecimentoViagem>.filterVisibleAbastecimentos(
        days: Int = AppConfig.DAYS_VISIBLE_IN_APP,
        now: Instant = Instant.now(),
    ): List<AbastecimentoViagem> = filter { isWithinVisibleWindow(it.data, days, appZoneId, now) }
}
