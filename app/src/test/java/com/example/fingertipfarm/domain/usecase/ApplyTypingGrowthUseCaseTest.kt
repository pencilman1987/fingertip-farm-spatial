package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.PlotStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplyTypingGrowthUseCaseTest {
    private val useCase = ApplyTypingGrowthUseCase()
    private val interact = InteractWithPlotUseCase()

    @Test
    fun firstMatchingCharacterPlantsItsKeyboardPlot() {
        val result = useCase(FarmDefaults.initialState(), "", "q")
        val qPlot = result.state.plots.first { it.id == "plot_13" }

        assertEquals(PlotStatus.GROWING, qPlot.status)
        assertEquals(0, qPlot.growth)
        assertEquals(1, result.plantedCount)
        assertEquals(1, result.state.typedCharacterTotal)
    }

    @Test
    fun repeatedMatchingCharacterGrowsTheSamePlot() {
        val result = useCase(FarmDefaults.initialState(), "", "qq")

        assertEquals(1, result.state.plots.first { it.id == "plot_13" }.growth)
        assertEquals(1, result.plantedCount)
        assertEquals(1, result.appliedGrowth)
    }

    @Test
    fun deletionDoesNotCreateAnotherFarmInteraction() {
        val planted = useCase(FarmDefaults.initialState(), "", "q").state
        val result = useCase(planted, "q", "")

        assertEquals(0, result.processedKeyCount)
        assertEquals(0, result.state.plots.first { it.id == "plot_13" }.growth)
        assertEquals(1, result.state.typedCharacterTotal)
    }

    @Test
    fun chineseCharactersUseDeterministicFallback() {
        val result = useCase(FarmDefaults.initialState(), "", "灵感")

        assertEquals(2, result.state.typedCharacterTotal)
        assertEquals(PlotStatus.GROWING, result.state.plots.first().status)
        assertEquals(1, result.state.plots.first().growth)
    }

    @Test
    fun growthStopsAtReadyThreshold() {
        val result = useCase(FarmDefaults.initialState(), "", "q".repeat(16))

        assertEquals(15, result.appliedGrowth)
        assertEquals(PlotStatus.READY, result.state.plots.first { it.id == "plot_13" }.status)
        assertEquals(1, result.newlyReadyCount)
    }

    @Test
    fun typingAReadyKeyHarvestsAndAwardsGold() {
        var ready = interact(FarmDefaults.initialState(), "plot_13").state
        repeat(15) { ready = interact(ready, "plot_13").state }

        val result = useCase(ready, "", "q")

        assertEquals(PlotStatus.EMPTY, result.state.plots.first { it.id == "plot_13" }.status)
        assertEquals(FarmDefaults.initialState().gold + 10, result.state.gold)
        assertEquals(1, result.harvestedCount)
        assertEquals(listOf("plot_13"), result.harvestedPlotIds)
    }

    @Test
    fun protectedReadyKeyDoesNotHarvest() {
        var ready = interact(FarmDefaults.initialState(), "plot_13").state
        repeat(15) { ready = interact(ready, "plot_13").state }

        val result = useCase(ready, "", "q", protectedReadyPlotIds = setOf("plot_13"))

        assertEquals(PlotStatus.READY, result.state.plots.first { it.id == "plot_13" }.status)
        assertEquals(0, result.harvestedCount)
    }

    @Test
    fun spaceStocksTheSpacePondWithoutInflatingCharacterTotal() {
        val result = useCase(FarmDefaults.initialState(), "", " ")

        assertEquals(PlotStatus.GROWING, result.state.plots.first { it.id == "plot_47" }.status)
        assertEquals(1, result.processedKeyCount)
        assertEquals(0, result.state.typedCharacterTotal)
    }
}
