package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.JournalMood
import com.example.fingertipfarm.domain.model.SpatialAmbience
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveSpatialAmbienceUseCaseTest {
    private val useCase = ResolveSpatialAmbienceUseCase()

    @Test
    fun calmJournalFollowsRealTimeAcrossDayParts() {
        assertEquals(SpatialAmbience.DAWN, useCase(6, JournalMood.CALM).ambience)
        assertEquals(SpatialAmbience.DAY, useCase(12, JournalMood.CALM).ambience)
        assertEquals(SpatialAmbience.DUSK, useCase(18, JournalMood.CALM).ambience)
        assertEquals(SpatialAmbience.NIGHT, useCase(23, JournalMood.CALM).ambience)
    }

    @Test
    fun strongJournalMoodOverridesClockWithoutAddingPenalty() {
        assertEquals(SpatialAmbience.DAY, useCase(23, JournalMood.GRATEFUL).ambience)
        assertEquals(SpatialAmbience.DUSK, useCase(10, JournalMood.THOUGHTFUL).ambience)
        assertEquals(SpatialAmbience.NIGHT, useCase(10, JournalMood.TIRED).ambience)
        assertEquals("随日记心情", useCase(10, JournalMood.JOYFUL).sourceLabel)
    }
}
