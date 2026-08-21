package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.PlotStatus

data class JournalWritingRewardResult(
    val state: FarmGameState,
    val appliedGrowth: Int,
)

class ApplyJournalWritingRewardUseCase(
    private val interactWithPlot: InteractWithPlotUseCase = InteractWithPlotUseCase(),
    private val charactersPerMilestone: Int = 20,
    private val maxMilestonesPerUpdate: Int = 3,
) {
    operator fun invoke(
        state: FarmGameState,
        entryId: String,
        nextBody: String,
        nowMillis: Long,
    ): JournalWritingRewardResult {
        val entry = state.journalEntries.firstOrNull { it.id == entryId }
            ?: return JournalWritingRewardResult(state, 0)
        val harvested = state.memoryCards.any { it.journalEntryId == entryId }
        val meaningfulCharacters = nextBody.codePoints().filter { !Character.isWhitespace(it) }.count().toInt()
        val availableMilestones = meaningfulCharacters / charactersPerMilestone.coerceAtLeast(1)
        val requestedGrowth =
            if (harvested) 0
            else (availableMilestones - entry.rewardedMilestones).coerceIn(0, maxMilestonesPerUpdate)

        var working = state
        var appliedGrowth = 0
        repeat(requestedGrowth) {
            val plot = working.plots.firstOrNull { it.id == entry.linkedPlotId }
            if (plot?.status == PlotStatus.GROWING) {
                val next = interactWithPlot(working, entry.linkedPlotId).state
                if (next != working) {
                    working = next
                    appliedGrowth += 1
                }
            }
        }
        val updatedEntry =
            entry.copy(
                body = nextBody,
                updatedAtMillis = nowMillis,
                rewardedMilestones = entry.rewardedMilestones + appliedGrowth,
            )
        working = working.copy(journalEntries = working.journalEntries.map { if (it.id == entryId) updatedEntry else it })
        return JournalWritingRewardResult(working, appliedGrowth)
    }
}
