package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.PlotState
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType

data class PlotInteractionResult(
    val state: FarmGameState,
    val message: String,
)

class InteractWithPlotUseCase(
    private val cropCareIncrement: Int = 1,
    private val fishCareIncrement: Int = 1,
) {
    operator fun invoke(
        state: FarmGameState,
        plotId: String,
        growthMultiplier: Int = 1,
    ): PlotInteractionResult {
        val plot = state.plots.firstOrNull { it.id == plotId }
            ?: return PlotInteractionResult(state, "未找到这块田")

        val result =
            when (plot.status) {
                PlotStatus.EMPTY -> plantOrStock(state, plot)
                PlotStatus.GROWING -> careFor(state, plot, growthMultiplier.coerceAtLeast(1))
                PlotStatus.READY -> harvest(state, plot)
            }

        return result
    }

    private fun plantOrStock(state: FarmGameState, plot: PlotState): PlotInteractionResult {
        val itemId = if (plot.type == PlotType.SOIL) state.activeCropId else state.activeFishId
        val item = FarmCatalog.find(itemId)
            ?: return PlotInteractionResult(state, "当前品种不可用")
        val planted =
            plot.copy(
                contentId = item.id,
                growth = 0,
                count = if (plot.type == PlotType.POND) 1 else 0,
                status = PlotStatus.GROWING,
            )
        val verb = if (plot.type == PlotType.SOIL) "播种" else "投苗"
        return PlotInteractionResult(state.replacePlot(planted), "$verb：${item.name}")
    }

    private fun careFor(
        state: FarmGameState,
        plot: PlotState,
        growthMultiplier: Int,
    ): PlotInteractionResult {
        val item = plot.contentId?.let(FarmCatalog::find)
            ?: return PlotInteractionResult(state, "这块田的品种数据已损坏")
        val increment = (if (plot.type == PlotType.SOIL) cropCareIncrement else fishCareIncrement) * growthMultiplier
        val newGrowth = plot.growth + increment
        val newCount =
            if (plot.type == PlotType.POND) {
                (newGrowth / item.growthNeeded + 1).coerceAtMost(99)
            } else {
                plot.count
            }
        val ready = if (plot.type == PlotType.POND) newCount > plot.count else newGrowth >= item.growthNeeded
        val updated =
            plot.copy(
                growth = if (plot.type == PlotType.SOIL) newGrowth.coerceAtMost(item.growthNeeded) else newGrowth,
                count = newCount,
                status = if (ready) PlotStatus.READY else PlotStatus.GROWING,
            )
        val message =
            when {
                plot.type == PlotType.POND && ready -> "${item.name}增殖到 ${newCount} 条，再点一次收获"
                ready -> "${item.name}已成熟，再点一次收获"
                growthMultiplier > 1 -> "高速照料 ${item.name}  +$increment"
                else -> "照料 ${item.name}  +$increment"
            }
        return PlotInteractionResult(state.replacePlot(updated), message)
    }

    private fun harvest(state: FarmGameState, plot: PlotState): PlotInteractionResult {
        val item = plot.contentId?.let(FarmCatalog::find)
            ?: return PlotInteractionResult(state, "这块田的品种数据已损坏")
        val quantity = if (plot.type == PlotType.POND) plot.count.coerceAtLeast(1) else 1
        val revenue = item.revenue * quantity
        val cleared = plot.copy(contentId = null, growth = 0, count = 0, status = PlotStatus.EMPTY)
        return PlotInteractionResult(
            state = state.copy(gold = state.gold + revenue).replacePlot(cleared),
            message = "收获 ${item.name}  +${revenue}G",
        )
    }

    private fun FarmGameState.replacePlot(updated: PlotState): FarmGameState =
        copy(plots = plots.map { if (it.id == updated.id) updated else it })
}
