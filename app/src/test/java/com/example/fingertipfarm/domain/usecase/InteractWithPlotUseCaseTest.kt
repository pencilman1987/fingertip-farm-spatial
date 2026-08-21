package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.PlotStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractWithPlotUseCaseTest {
    private val useCase = InteractWithPlotUseCase(cropCareIncrement = 15, fishCareIncrement = 12)

    @Test
    fun emptySoilPlantsActiveCrop() {
        val result = useCase(FarmDefaults.initialState(), "plot_1")
        val plot = result.state.plots.first { it.id == "plot_1" }

        assertEquals("carrot", plot.contentId)
        assertEquals(PlotStatus.GROWING, plot.status)
        assertTrue(result.message.contains("播种"))
    }

    @Test
    fun growingCropBecomesReadyAtThreshold() {
        val planted = useCase(FarmDefaults.initialState(), "plot_1").state
        val result = useCase(planted, "plot_1")
        val plot = result.state.plots.first { it.id == "plot_1" }

        assertEquals(15, plot.growth)
        assertEquals(PlotStatus.READY, plot.status)
    }

    @Test
    fun readyCropHarvestAddsGoldAndClearsPlot() {
        val planted = useCase(FarmDefaults.initialState(), "plot_1").state
        val ready = useCase(planted, "plot_1").state
        val result = useCase(ready, "plot_1")
        val plot = result.state.plots.first { it.id == "plot_1" }

        assertEquals(FarmDefaults.initialState().gold + 10, result.state.gold)
        assertEquals(PlotStatus.EMPTY, plot.status)
        assertEquals(null, plot.contentId)
    }

    @Test
    fun pondGrowthIncreasesFishCountAndHarvestPaysForEveryFish() {
        val pondId = "plot_36"
        val stocked = useCase(FarmDefaults.initialState(), pondId).state
        val ready = useCase(stocked, pondId).state
        val pond = ready.plots.first { it.id == pondId }

        assertEquals(2, pond.count)
        assertEquals(PlotStatus.READY, pond.status)

        val harvested = useCase(ready, pondId)
        assertEquals(FarmDefaults.initialState().gold + 16, harvested.state.gold)
    }
}
