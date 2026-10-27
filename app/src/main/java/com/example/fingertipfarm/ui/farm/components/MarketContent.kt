package com.example.fingertipfarm.ui.farm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.fingertipfarm.domain.model.CatalogItem
import com.example.fingertipfarm.domain.model.CatalogKind
import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.ui.theme.FarmShapes
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text

@Composable
fun MarketContent(
    game: FarmGameState,
    onPurchase: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MarketColumn(
                title = "植物",
                items = FarmCatalog.crops.filterNot { game.owns(it) },
                game = game,
                onPurchase = onPurchase,
                modifier = Modifier.weight(1f),
            )
            MarketColumn(
                title = "鱼类",
                items = FarmCatalog.fish.filterNot { game.owns(it) },
                game = game,
                onPurchase = onPurchase,
                modifier = Modifier.weight(1f),
            )
            MarketColumn(
                title = "伙伴",
                items = FarmCatalog.partners.filterNot { game.owns(it) },
                game = game,
                onPurchase = onPurchase,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MarketColumn(
    title: String,
    items: List<CatalogItem>,
    game: FarmGameState,
    onPurchase: (CatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = PicoTheme.typography.titleMedium)
        if (items.isEmpty()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(FarmShapes.Card)
                        .background(PicoTheme.colorScheme.fillSecondary)
                        .padding(14.dp),
            ) {
                Text("已全部拥有", style = PicoTheme.typography.labelLarge)
                Text("已收入物种仓库", style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
            }
        }
        items.forEach { item ->
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(FarmShapes.Card)
                        .background(PicoTheme.colorScheme.fillSecondary)
                        .padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text("${item.emoji}  ${item.name}", style = PicoTheme.typography.labelLarge)
                val detail =
                    if (item.kind == CatalogKind.PARTNER) item.description
                    else "成熟 ${item.growthNeeded} · 收益 ${item.revenue}G"
                Text(detail, style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
                Button(
                    onClick = { onPurchase(item) },
                ) {
                    Text("${item.price}G  解锁")
                }
            }
        }
    }
}

private fun FarmGameState.owns(item: CatalogItem): Boolean =
    when (item.kind) {
        CatalogKind.CROP -> item.id in unlockedCropIds
        CatalogKind.FISH -> item.id in unlockedFishIds
        CatalogKind.PARTNER -> item.id in ownedPartnerIds
    }
