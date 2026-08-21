package com.example.fingertipfarm.ui.farm

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fingertipfarm.R
import com.example.fingertipfarm.domain.model.FarmTheme
import com.example.fingertipfarm.domain.model.SpatialAmbience
import com.example.fingertipfarm.domain.model.SpatialExperienceMode
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.SegmentControl
import com.pico.spatial.ui.design.SegmentItem
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material

@Composable
fun SpatialFarmExperience(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val farmViewModel: FarmViewModel = viewModel(factory = FarmViewModel.factory(context))
    val state by farmViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        farmViewModel.onEvent(FarmEvent.SelectSpatialMode(SpatialExperienceMode.WRITING))
    }

    SpatialFarmContent(
        state = state,
        onEvent = farmViewModel::onEvent,
        modifier = Modifier.fillMaxSize().then(modifier),
    )
}

@Composable
internal fun SpatialFarmContent(
    state: FarmUiState,
    onEvent: (FarmEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayTheme =
        when (state.spatialAmbience.ambience) {
            SpatialAmbience.DAWN, SpatialAmbience.DAY -> FarmTheme.DAY
            SpatialAmbience.DUSK, SpatialAmbience.NIGHT -> FarmTheme.NIGHT
        }
    val displayState = state.copy(game = state.game.copy(theme = displayTheme))

    Column(
        modifier = Modifier.fillMaxSize().then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpatialModeBar(
                selectedMode = state.spatialMode,
                ambienceLabel = "${state.spatialAmbience.ambience.label} · ${state.spatialAmbience.sourceLabel}",
                onSelectMode = { onEvent(FarmEvent.SelectSpatialMode(it)) },
            )
            SharedSpaceMemoryStrip(state = state)
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .backgroundMaterial(enable = true, style = Material.Thick)
                    .padding(8.dp),
        ) {
            FarmContent(
                state = displayState,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SharedSpaceMemoryStrip(
    state: FarmUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpatialMemoryObject(
            imageRes = R.drawable.spatial_memory_frame,
            contentDescription = "记忆相框",
            label = "相框 · ${state.game.journalEntries.count { it.localImageUri != null }}",
            emphasized = state.spatialMode != SpatialExperienceMode.WRITING,
            compact = true,
        )
        SpatialMemoryObject(
            imageRes = R.drawable.spatial_memory_fruit,
            contentDescription = "记忆果实",
            label = "果实 · ${state.game.memoryCards.size}",
            emphasized = state.spatialMode == SpatialExperienceMode.REVIEW,
            compact = true,
        )
        SpatialMemoryObject(
            imageRes = R.drawable.spatial_journal_plant,
            contentDescription = "日记植物",
            label = "植物 · ${state.game.journalEntries.size}",
            emphasized = state.spatialMode != SpatialExperienceMode.REVIEW,
            compact = true,
        )
    }
}

@Composable
private fun SpatialModeBar(
    selectedMode: SpatialExperienceMode,
    ambienceLabel: String,
    onSelectMode: (SpatialExperienceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            Modifier
                .width(620.dp)
                .clip(RoundedCornerShape(20.dp))
                .backgroundMaterial(enable = true, style = Material.Thick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SegmentControl(modifier = Modifier.width(580.dp)) {
            SpatialExperienceMode.entries.forEach { mode ->
                SegmentItem(
                    selected = selectedMode == mode,
                    onClick = { onSelectMode(mode) },
                    title = { Text(mode.label) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = ambienceLabel,
            color = PicoTheme.colorScheme.labelSecondary,
            style = PicoTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun SpatialMemoryObject(
    imageRes: Int,
    contentDescription: String,
    label: String,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val objectSize =
        when {
            compact && emphasized -> 82.dp
            compact -> 72.dp
            emphasized -> 172.dp
            else -> 148.dp
        }
    val containerWidth = if (compact) 116.dp else objectSize
    val containerHeight = if (compact) 116.dp else objectSize + 38.dp
    val imageSize = if (compact) objectSize else objectSize - 28.dp

    Column(
        modifier =
            Modifier
                .size(containerWidth, containerHeight)
                .spatialHoverEffect()
                .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(imageSize),
        )
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .backgroundMaterial(enable = true, style = Material.Thin)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = label,
                color = PicoTheme.colorScheme.labelPrimary,
                style = PicoTheme.typography.labelMedium,
            )
        }
    }
}
