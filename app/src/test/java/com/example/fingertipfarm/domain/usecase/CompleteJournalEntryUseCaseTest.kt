package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteJournalEntryUseCaseTest {
    private val createEntry = CreateJournalEntryUseCase()
    private val writingReward = ApplyJournalWritingRewardUseCase()
    private val useCase = CompleteJournalEntryUseCase()

    @Test
    fun blankEntryCannotBeCompleted() {
        val created = createEntry(FarmDefaults.initialState(), 1000L, "2026-08-04")

        val result = useCase(created.state, requireNotNull(created.entry).id, 2000L)

        assertFalse(result.completed)
        assertEquals(null, result.state.journalEntries.single().completedAtMillis)
    }

    @Test
    fun completionAwardsThreeGrowthOnlyOnce() {
        val created = createEntry(FarmDefaults.initialState(), 1000L, "2026-08-04")
        val entry = requireNotNull(created.entry)
        val written = writingReward(created.state, entry.id, "今天很好", 1500L).state

        val first = useCase(written, entry.id, 2000L)
        val second = useCase(first.state, entry.id, 3000L)

        assertTrue(first.completed)
        assertEquals(3, first.appliedGrowth)
        assertFalse(second.completed)
        assertEquals(3, first.state.plots.first { it.id == entry.linkedPlotId }.growth)
        assertEquals(3, second.state.plots.first { it.id == entry.linkedPlotId }.growth)
    }
}
