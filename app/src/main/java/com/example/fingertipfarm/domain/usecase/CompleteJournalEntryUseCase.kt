package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.PlotStatus

data class JournalCompletionResult(
    val state: FarmGameState,
    val completed: Boolean,
    val appliedGrowth: Int,
    val message: String,
)

class CompleteJournalEntryUseCase(
    private val interactWithPlot: InteractWithPlotUseCase = InteractWithPlotUseCase(),
    private val completionGrowth: Int = 3,
) {
    operator fun invoke(
        state: FarmGameState,
        entryId: String,
        nowMillis: Long,
    ): JournalCompletionResult {
        val entry = state.journalEntries.firstOrNull { it.id == entryId }
            ?: return JournalCompletionResult(state, false, 0, "没有正在编辑的日记")
        if (entry.body.isBlank()) return JournalCompletionResult(state, false, 0, "先写下一点内容再完成记录吧")
        if (entry.completedAtMillis != null) return JournalCompletionResult(state, false, 0, "这篇日记已经完成，仍可继续补写")

        var working = state
        var appliedGrowth = 0
        repeat(completionGrowth.coerceAtLeast(0)) {
            val plot = working.plots.firstOrNull { it.id == entry.linkedPlotId }
            if (plot?.status == PlotStatus.GROWING) {
                val next = interactWithPlot(working, entry.linkedPlotId).state
                if (next != working) {
                    working = next
                    appliedGrowth += 1
                }
            }
        }
        val completedEntry = entry.copy(completedAtMillis = nowMillis, updatedAtMillis = nowMillis)
        working = working.copy(journalEntries = working.journalEntries.map { if (it.id == entryId) completedEntry else it })
        val ready = working.plots.firstOrNull { it.id == entry.linkedPlotId }?.status == PlotStatus.READY
        val message = if (ready) "记忆作物已经成熟，回农场收获记忆卡吧" else "日记已完成，记忆作物成长 +$appliedGrowth"
        return JournalCompletionResult(working, true, appliedGrowth, message)
    }
}
