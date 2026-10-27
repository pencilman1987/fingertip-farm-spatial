package com.example.fingertipfarm.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FarmDefaultsTest {
    @Test
    fun `keyboard plots follow original web layout`() {
        val state = FarmDefaults.initialState()

        assertEquals(47, state.plots.size)
        assertEquals(47, FarmDefaults.KeyboardLayout.flatten().size)
        assertEquals((0 until 47).toSet(), FarmDefaults.SheepPatrolPlotIndices.toSet())
        assertEquals(listOf(0, 1, 2, 3), FarmDefaults.SheepPatrolPlotIndices.take(4))
        assertEquals(listOf(23, 22, 21, 20), FarmDefaults.SheepPatrolPlotIndices.drop(12).take(4))
        assertEquals(2, state.plots.count { it.type == PlotType.POND })
        assertEquals(
            listOf("ENTER", "SPACE"),
            FarmDefaults.KeyboardLayout.flatten().zip(state.plots)
                .filter { (_, plot) -> plot.type == PlotType.POND }
                .map { (key, _) -> key },
        )
    }
}
