package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplyJournalWritingRewardUseCaseTest {
    private val createEntry = CreateJournalEntryUseCase()
    private val useCase = ApplyJournalWritingRewardUseCase()

    @Test
    fun everyTwentyMeaningfulCharactersGrowLinkedCropOnce() {
        val created = createEntry(FarmDefaults.initialState(), 1000L, "2026-08-04")
        val entry = requireNotNull(created.entry)

        val result = useCase(created.state, entry.id, "记".repeat(20), 2000L)

        assertEquals(1, result.appliedGrowth)
        assertEquals(1, result.state.plots.first { it.id == entry.linkedPlotId }.growth)
        assertEquals(1, result.state.journalEntries.single().rewardedMilestones)
    }

    @Test
    fun aLargePasteIsCappedToThreeMilestonesPerUpdate() {
        val created = createEntry(FarmDefaults.initialState(), 1000L, "2026-08-04")
        val entry = requireNotNull(created.entry)

        val result = useCase(created.state, entry.id, "a".repeat(500), 2000L)

        assertEquals(3, result.appliedGrowth)
        assertEquals(3, result.state.plots.first { it.id == entry.linkedPlotId }.growth)
    }
}
