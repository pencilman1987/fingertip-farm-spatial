package com.example.fingertipfarm.ui.farm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fingertipfarm.R
import com.example.fingertipfarm.ui.navigation.FarmSection
import com.example.fingertipfarm.ui.theme.FarmColors
import com.example.fingertipfarm.ui.theme.FarmShapes
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.SideNavigation
import com.pico.spatial.ui.design.SideNavigationItem
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.haptic.controllerHapticFeedback
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect

@Composable
fun FarmNavigation(
    selected: FarmSection,
    onSelect: (FarmSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    SideNavigation(
        modifier = Modifier.width(212.dp).fillMaxHeight().then(modifier),
        header = {
            Column(modifier = Modifier.padding(start = 8.dp, top = 14.dp, bottom = 12.dp)) {
                Text("农场地图", style = PicoTheme.typography.titleLarge)
                Text("选择一座建筑", style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
            }
        },
    ) {
        FarmSection.entries.forEach { section ->
            val interactionSource = remember(section) { MutableInteractionSource() }
            SideNavigationItem(
                selected = selected == section,
                modifier =
                    Modifier
                        .spatialHoverEffect()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            onClick = { onSelect(section) },
                        )
                        .controllerHapticFeedback(interactionSource = interactionSource),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(section.illustrationRes()),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp).clip(FarmShapes.Message),
                        contentScale = if (section == FarmSection.FARM) ContentScale.Crop else ContentScale.Fit,
                    )
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            section.title,
                            style = PicoTheme.typography.labelLarge,
                            color = if (selected == section) FarmColors.WoodDark else PicoTheme.colorScheme.labelPrimary,
                        )
                        Text(
                            section.subtitle,
                            style = PicoTheme.typography.bodySmall,
                            color = PicoTheme.colorScheme.labelSecondary,
                        )
                    }
                }
            }
        }
    }
}

@DrawableRes
private fun FarmSection.illustrationRes(): Int =
    when (this) {
        FarmSection.FARM -> R.drawable.farm_world_day
        FarmSection.INVENTORY -> R.drawable.farm_warehouse
        FarmSection.MARKET -> R.drawable.farm_market
        FarmSection.WORKSHOP -> R.drawable.farm_workshop
    }
