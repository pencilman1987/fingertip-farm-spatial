package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.JournalEntry
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType

data class JournalCreationResult(
    val state: FarmGameState,
    val entry: JournalEntry?,
    val message: String,
)

class CreateJournalEntryUseCase(
    private val interactWithPlot: InteractWithPlotUseCase = InteractWithPlotUseCase(),
) {
    operator fun invoke(
        state: FarmGameState,
        nowMillis: Long,
        dateKey: String,
        dailyPrompt: String = "",
    ): JournalCreationResult {
        val plot = state.plots.firstOrNull { it.type == PlotType.SOIL && it.status == PlotStatus.EMPTY }
            ?: return JournalCreationResult(state, null, "农田已经种满，先收获一株记忆作物吧")
        val planted = interactWithPlot(state, plot.id).state
        val cropId = planted.plots.first { it.id == plot.id }.contentId ?: state.activeCropId
        val entry =
            JournalEntry(
                id = "journal_${nowMillis}_${state.journalEntries.size + 1}",
                dateKey = dateKey,
                title = "$dateKey 日记",
                body = "",
                cropId = cropId,
                linkedPlotId = plot.id,
                createdAtMillis = nowMillis,
                updatedAtMillis = nowMillis,
                dailyPrompt = dailyPrompt,
            )
        val next =
            planted.copy(
                journalEntries = planted.journalEntries + entry,
                activeJournalEntryId = entry.id,
            )
        return JournalCreationResult(next, entry, "已种下今天的记忆作物")
    }
}
