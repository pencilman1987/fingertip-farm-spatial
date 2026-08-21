package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.FarmSeason
import com.example.fingertipfarm.domain.model.JournalEntry
import com.example.fingertipfarm.domain.model.MemoryCard
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflectionGardenUseCaseTest {
    private val useCase = ReflectionGardenUseCase()
    private val zone = ZoneId.of("UTC")

    @Test
    fun emptyWeekStaysGentleAndHasNoPenalty() {
        val summary = useCase(FarmDefaults.initialState(), LocalDate.of(2026, 8, 4), zone)
        assertEquals(0, summary.weeklyEntryCount)
        assertTrue(summary.gentleMessage.contains("想写的时候再来"))
        assertEquals(FarmSeason.SUMMER, summary.season)
    }

    @Test
    fun countsOnlyCurrentWeekAndSeason() {
        val current = entry("current", "2026-08-04", "今天写下的内容", completed = true)
        val old = entry("old", "2026-06-02", "较早的记录", completed = false)
        val harvested = LocalDate.of(2026, 8, 5).atStartOfDay(zone).toInstant().toEpochMilli()
        val state =
            FarmDefaults.initialState().copy(
                journalEntries = listOf(current, old),
                memoryCards = listOf(MemoryCard("memory_1", current.id, harvested)),
            )

        val summary = useCase(state, LocalDate.of(2026, 8, 4), zone)

        assertEquals(1, summary.weeklyEntryCount)
        assertEquals(1, summary.weeklyCompletedCount)
        assertEquals(1, summary.weeklyMemoryCount)
        assertEquals(2, summary.seasonEntryCount)
    }

    private fun entry(id: String, date: String, body: String, completed: Boolean) =
        JournalEntry(
            id = id,
            dateKey = date,
            title = id,
            body = body,
            cropId = "carrot",
            linkedPlotId = "plot_1",
            createdAtMillis = 1L,
            updatedAtMillis = 1L,
            completedAtMillis = if (completed) 2L else null,
        )
}
