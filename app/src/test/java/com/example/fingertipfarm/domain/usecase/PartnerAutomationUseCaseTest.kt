package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PartnerAutomationUseCaseTest {
    private val useCase = PartnerAutomationUseCase()

    @Test
    fun sheepHarvestsOneReadyCropAndAddsRevenue() {
        val initial = FarmDefaults.initialState()
        val plot = initial.plots.first { it.type == PlotType.SOIL }
        val state =
            initial.copy(
                gold = 80,
                ownedPartnerIds = setOf("sheep"),
                plots = initial.plots.map { if (it.id == plot.id) it.copy(contentId = "carrot", growth = 15, status = PlotStatus.READY) else it },
            )

        val result = useCase.runSheepPatrol(state, plot.id, emptySet())

        assertEquals(90, result.state.gold)
        assertEquals(PlotStatus.EMPTY, result.state.plots.first { it.id == plot.id }.status)
        assertEquals(listOf(plot.id), result.harvestedPlotIds)
        assertTrue(result.message.orEmpty().contains("小羊巡逻收获"))
    }

    @Test
    fun sheepDoesNotHarvestProtectedJournalCrop() {
        val initial = FarmDefaults.initialState()
        val plot = initial.plots.first { it.type == PlotType.SOIL }
        val state =
            initial.copy(
                ownedPartnerIds = setOf("sheep"),
                plots = initial.plots.map { if (it.id == plot.id) it.copy(contentId = "carrot", growth = 15, status = PlotStatus.READY) else it },
            )

        val result = useCase.runSheepPatrol(state, plot.id, setOf(plot.id))

        assertEquals(state, result.state)
        assertTrue(result.harvestedPlotIds.isEmpty())
    }

    @Test
    fun catCatchesOneFishInsteadOfEmptyingWholePond() {
        val initial = FarmDefaults.initialState()
        val pond = initial.plots.first { it.type == PlotType.POND }
        val state =
            initial.copy(
                gold = 80,
                ownedPartnerIds = setOf("cat"),
                plots = initial.plots.map { if (it.id == pond.id) it.copy(contentId = "goldfish", growth = 24, count = 3, status = PlotStatus.READY) else it },
            )

        val result = useCase.runCatFishing(state)

        assertEquals(88, result.state.gold)
        assertEquals(2, result.state.plots.first { it.id == pond.id }.count)
        assertTrue(result.message.orEmpty().contains("猫咪从池塘钓起"))
    }
}
