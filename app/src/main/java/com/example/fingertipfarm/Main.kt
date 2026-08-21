package com.example.fingertipfarm

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.CompositionLocalProvider
import com.example.fingertipfarm.ui.farm.SpatialFarmExperience
import com.example.fingertipfarm.ui.theme.FarmSilentIndication
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultWindowContainer
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultWindowContainer {
            PicoTheme {
                CompositionLocalProvider(LocalIndication provides FarmSilentIndication) {
                    SpatialFarmExperience()
                }
            }
        }
    }
