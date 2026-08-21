package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.JournalMood
import com.example.fingertipfarm.domain.model.SpatialAmbience
import com.example.fingertipfarm.domain.model.SpatialAmbienceState

class ResolveSpatialAmbienceUseCase {
    operator fun invoke(
        localHour: Int,
        activeMood: JournalMood?,
    ): SpatialAmbienceState =
        when (activeMood) {
            JournalMood.JOYFUL, JournalMood.GRATEFUL -> SpatialAmbienceState(SpatialAmbience.DAY, "随日记心情")
            JournalMood.THOUGHTFUL -> SpatialAmbienceState(SpatialAmbience.DUSK, "随日记心情")
            JournalMood.TIRED -> SpatialAmbienceState(SpatialAmbience.NIGHT, "随日记心情")
            JournalMood.CALM, null -> SpatialAmbienceState(timeAmbience(localHour), "随真实时间")
        }

    private fun timeAmbience(localHour: Int): SpatialAmbience =
        when (Math.floorMod(localHour, 24)) {
            in 5..7 -> SpatialAmbience.DAWN
            in 8..16 -> SpatialAmbience.DAY
            in 17..19 -> SpatialAmbience.DUSK
            else -> SpatialAmbience.NIGHT
        }
}
