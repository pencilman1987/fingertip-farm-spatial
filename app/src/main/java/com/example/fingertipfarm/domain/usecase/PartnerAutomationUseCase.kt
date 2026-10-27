package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType

data class PartnerAutomationResult(
    val state: FarmGameState,
    val message: String? = null,
    val harvestedPlotIds: List<String> = emptyList(),
)

class PartnerAutomationUseCase(
    private val interactWithPlot: InteractWithPlotUseCase = InteractWithPlotUseCase(),
) {
    fun runSheepPatrol(
        state: FarmGameState,
        targetPlotId: String,
        protectedPlotIds: Set<String>,
    ): PartnerAutomationResult {
        if ("sheep" !in state.ownedPartnerIds) return PartnerAutomationResult(state)
        val target =
            state.plots.firstOrNull { it.id == targetPlotId }
                ?.takeIf {
                    it.type == PlotType.SOIL &&
                        it.status == PlotStatus.READY &&
                        it.id !in protectedPlotIds
                } ?: return PartnerAutomationResult(state)
        val itemName = target.contentId?.let(FarmCatalog::find)?.name ?: "作物"
        val result = interactWithPlot(state, target.id)
        val earned = result.state.gold - state.gold
        return PartnerAutomationResult(
            state = result.state,
            message = "小羊巡逻收获了$itemName  +${earned}G",
            harvestedPlotIds = listOf(target.id),
        )
    }

    fun runCatFishing(state: FarmGameState): PartnerAutomationResult {
        if ("cat" !in state.ownedPartnerIds) return PartnerAutomationResult(state)
        val target =
            state.plots
                .filter { it.type == PlotType.POND && it.contentId != null && it.count > 0 }
                .maxByOrNull { it.count }
                ?: return PartnerAutomationResult(state)
        val fish = target.contentId?.let(FarmCatalog::find) ?: return PartnerAutomationResult(state)
        val remaining = target.count - 1
        val updated =
            if (remaining <= 0) {
                target.copy(contentId = null, growth = 0, count = 0, status = PlotStatus.EMPTY)
            } else {
                target.copy(count = remaining)
            }
        return PartnerAutomationResult(
            state =
                state.copy(
                    gold = state.gold + fish.revenue,
                    plots = state.plots.map { if (it.id == updated.id) updated else it },
                ),
            message = "猫咪从池塘钓起${fish.name}  +${fish.revenue}G",
        )
    }
}
