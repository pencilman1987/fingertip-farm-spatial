package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateMemoryCardUseCaseTest {
    private val createEntry = CreateJournalEntryUseCase()
    private val writingReward = ApplyJournalWritingRewardUseCase()
    private val completeEntry = CompleteJournalEntryUseCase()
    private val interact = InteractWithPlotUseCase()
    private val useCase = CreateMemoryCardUseCase()

    @Test
    fun completedEntryCreatesOneCardWhenItsPlotIsHarvested() {
        val created = createEntry(FarmDefaults.initialState(), 1000L, "2026-08-04")
        val entry = requireNotNull(created.entry)
        var state = writingReward(created.state, entry.id, "完成的日记", 1500L).state
        state = completeEntry(state, entry.id, 2000L).state
        while (state.plots.first { it.id == entry.linkedPlotId }.status.name == "GROWING") {
            state = interact(state, entry.linkedPlotId).state
        }
        state = interact(state, entry.linkedPlotId).state

        val result = useCase(state, listOf(entry.linkedPlotId), 3000L)

        assertEquals(1, result.createdCount)
        assertEquals(entry.id, result.state.memoryCards.single().journalEntryId)
    }

    @Test
    fun unfinishedEntryNeverCreatesMemoryCard() {
        val created = createEntry(FarmDefaults.initialState(), 1000L, "2026-08-04")
        val entry = requireNotNull(created.entry)

        val result = useCase(created.state, listOf(entry.linkedPlotId), 3000L)

        assertEquals(0, result.createdCount)
        assertEquals(emptyList<Any>(), result.state.memoryCards)
    }
}
