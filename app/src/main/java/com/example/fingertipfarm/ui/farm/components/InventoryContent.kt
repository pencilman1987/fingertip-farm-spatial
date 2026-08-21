package com.example.fingertipfarm.ui.farm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.fingertipfarm.domain.model.CatalogItem
import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.ui.theme.FarmShapes
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.design.ToggleableChip

@Composable
fun InventoryContent(
    game: FarmGameState,
    onSelectSpecies: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            SpeciesPanel(
                title = "植物种子",
                items = FarmCatalog.crops.filter { it.id in game.unlockedCropIds },
                selectedId = game.activeCropId,
                onSelect = onSelectSpecies,
                modifier = Modifier.weight(1f),
            )
            SpeciesPanel(
                title = "鱼苗品种",
                items = FarmCatalog.fish.filter { it.id in game.unlockedFishIds },
                selectedId = game.activeFishId,
                onSelect = onSelectSpecies,
                modifier = Modifier.weight(1f),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(FarmShapes.Card)
                    .background(PicoTheme.colorScheme.fillSecondary)
                    .padding(14.dp),
        ) {
            Text("已招募合伙人", style = PicoTheme.typography.titleMedium)
            if (game.ownedPartnerIds.isEmpty()) {
                Text(
                    "还没有伙伴。去集市看看吧。",
                    modifier = Modifier.padding(top = 10.dp),
                    style = PicoTheme.typography.bodyMedium,
                    color = PicoTheme.colorScheme.labelSecondary,
                )
            } else {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FarmCatalog.partners.filter { it.id in game.ownedPartnerIds }.forEach { partner ->
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clip(FarmShapes.Message)
                                    .background(PicoTheme.colorScheme.fillTertiary)
                                    .padding(14.dp),
                        ) {
                            Column {
                                Text("${partner.emoji}  ${partner.name}", style = PicoTheme.typography.labelLarge)
                                Text(
                                    partner.description.substringBefore("，"),
                                    modifier = Modifier.padding(top = 6.dp),
                                    style = PicoTheme.typography.bodySmall,
                                    color = PicoTheme.colorScheme.labelSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeciesPanel(
    title: String,
    items: List<CatalogItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(FarmShapes.Card)
                .background(PicoTheme.colorScheme.fillSecondary)
                .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(title, style = PicoTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { item ->
                ToggleableChip(
                    label = { Text("${item.emoji} ${item.name}") },
                    isToggleOn = item.id == selectedId,
                    onClick = { onSelect(item.id) },
                )
            }
        }
    }
}
