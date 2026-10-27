package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectSpeciesUseCaseTest {
    private val useCase = SelectSpeciesUseCase()

    @Test
    fun unlockedCropCanBeSelected() {
        val state = FarmDefaults.initialState().copy(unlockedCropIds = setOf("carrot", "tomato"))
        val result = useCase(state, "tomato")

        assertTrue(result.selected)
        assertEquals("tomato", result.state.activeCropId)
    }

    @Test
    fun lockedFishCannotBeSelected() {
        val state = FarmDefaults.initialState()
        val result = useCase(state, "shark")

        assertFalse(result.selected)
        assertEquals("goldfish", result.state.activeFishId)
    }

    @Test
    fun unknownSpeciesDoesNotChangeState() {
        val state = FarmDefaults.initialState()
        val result = useCase(state, "missing")

        assertFalse(result.selected)
        assertEquals(state, result.state)
    }
}
