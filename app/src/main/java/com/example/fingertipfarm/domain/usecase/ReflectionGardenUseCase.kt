package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.FarmSeason
import com.example.fingertipfarm.domain.model.ReflectionGardenSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class ReflectionGardenUseCase {
    operator fun invoke(
        state: FarmGameState,
        today: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): ReflectionGardenSummary {
        val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val weekEnd = weekStart.plusDays(6)
        val (season, seasonStart, seasonEnd) = seasonRange(today)
        val datedEntries = state.journalEntries.mapNotNull { entry -> parseEntryDate(entry.dateKey)?.let { it to entry } }
        val weeklyEntries = datedEntries.filter { (date) -> !date.isBefore(weekStart) && !date.isAfter(weekEnd) }.map { it.second }
        val seasonalEntries = datedEntries.filter { (date) -> !date.isBefore(seasonStart) && !date.isAfter(seasonEnd) }.map { it.second }
        val weeklyMemories =
            state.memoryCards.count { card ->
                val date = Instant.ofEpochMilli(card.harvestedAtMillis).atZone(zoneId).toLocalDate()
                !date.isBefore(weekStart) && !date.isAfter(weekEnd)
            }
        val seasonalMemories =
            state.memoryCards.count { card ->
                val date = Instant.ofEpochMilli(card.harvestedAtMillis).atZone(zoneId).toLocalDate()
                !date.isBefore(seasonStart) && !date.isAfter(seasonEnd)
            }
        val message =
            when {
                weeklyEntries.isEmpty() -> "这一周还是一块安静的土地，想写的时候再来。"
                weeklyEntries.size == 1 -> "这一篇已经让本周的花园有了颜色。"
                weeklyMemories > 0 -> "你写下了 ${weeklyEntries.size} 篇，也收获了 $weeklyMemories 张可以回看的记忆。"
                else -> "你写下了 ${weeklyEntries.size} 篇，每一篇都在慢慢生长。"
            }
        return ReflectionGardenSummary(
            season = season,
            seasonEntryCount = seasonalEntries.size,
            seasonMemoryCount = seasonalMemories,
            weeklyEntryCount = weeklyEntries.size,
            weeklyCompletedCount = weeklyEntries.count { it.completedAtMillis != null },
            weeklyMemoryCount = weeklyMemories,
            weeklyCharacterCount = weeklyEntries.sumOf { it.body.count { char -> !char.isWhitespace() } },
            gentleMessage = message,
        )
    }

    private fun parseEntryDate(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrNull()

    private fun seasonRange(today: LocalDate): Triple<FarmSeason, LocalDate, LocalDate> =
        when (today.monthValue) {
            in 3..5 -> Triple(FarmSeason.SPRING, LocalDate.of(today.year, 3, 1), LocalDate.of(today.year, 5, 31))
            in 6..8 -> Triple(FarmSeason.SUMMER, LocalDate.of(today.year, 6, 1), LocalDate.of(today.year, 8, 31))
            in 9..11 -> Triple(FarmSeason.AUTUMN, LocalDate.of(today.year, 9, 1), LocalDate.of(today.year, 11, 30))
            12 -> Triple(FarmSeason.WINTER, LocalDate.of(today.year, 12, 1), LocalDate.of(today.year + 1, 2, 1).with(TemporalAdjusters.lastDayOfMonth()))
            else -> Triple(FarmSeason.WINTER, LocalDate.of(today.year - 1, 12, 1), LocalDate.of(today.year, 2, 1).with(TemporalAdjusters.lastDayOfMonth()))
        }
}
