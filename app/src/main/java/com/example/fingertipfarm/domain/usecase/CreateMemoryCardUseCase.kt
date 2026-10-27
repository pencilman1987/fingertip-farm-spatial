package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.MemoryCard

data class MemoryHarvestResult(
    val state: FarmGameState,
    val createdCount: Int,
)

class CreateMemoryCardUseCase {
    operator fun invoke(
        state: FarmGameState,
        harvestedPlotIds: Collection<String>,
        nowMillis: Long,
    ): MemoryHarvestResult {
        if (harvestedPlotIds.isEmpty()) return MemoryHarvestResult(state, 0)
        val existingEntryIds = state.memoryCards.mapTo(mutableSetOf()) { it.journalEntryId }
        val entries =
            state.journalEntries.filter {
                it.linkedPlotId in harvestedPlotIds &&
                    it.completedAtMillis != null &&
                    it.id !in existingEntryIds
            }
        if (entries.isEmpty()) return MemoryHarvestResult(state, 0)
        val cards =
            entries.mapIndexed { index, entry ->
                MemoryCard(
                    id = "memory_${nowMillis}_${state.memoryCards.size + index + 1}",
                    journalEntryId = entry.id,
                    harvestedAtMillis = nowMillis,
                )
            }
        return MemoryHarvestResult(state.copy(memoryCards = state.memoryCards + cards), cards.size)
    }
}
