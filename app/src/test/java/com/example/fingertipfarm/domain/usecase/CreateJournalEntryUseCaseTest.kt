package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateJournalEntryUseCaseTest {
    private val useCase = CreateJournalEntryUseCase()

    @Test
    fun createsDatedEntryAndPlantsFirstEmptySoilPlot() {
        val result = useCase(FarmDefaults.initialState(), 1000L, "2026-08-04")

        assertEquals("2026-08-04", result.entry?.dateKey)
        assertEquals("plot_1", result.entry?.linkedPlotId)
        assertEquals(PlotStatus.GROWING, result.state.plots.first().status)
        assertEquals(result.entry?.id, result.state.activeJournalEntryId)
    }

    @Test
    fun refusesCreationWhenEverySoilPlotIsOccupied() {
        val initial = FarmDefaults.initialState()
        val full =
            initial.copy(
                plots = initial.plots.map {
                    if (it.type == PlotType.SOIL) it.copy(contentId = "carrot", status = PlotStatus.GROWING) else it
                },
            )

        val result = useCase(full, 1000L, "2026-08-04")

        assertNull(result.entry)
        assertTrue(result.message.contains("种满"))
    }
}
