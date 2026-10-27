package com.example.fingertipfarm.ui.farm.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.ui.theme.FarmColors
import com.example.fingertipfarm.ui.theme.FarmShapes
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.haptic.controllerHapticFeedback
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect

@Composable
fun FarmHeader(
    game: FarmGameState,
    message: String?,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val crop = FarmCatalog.find(game.activeCropId)
    val fish = FarmCatalog.find(game.activeFishId)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(FarmShapes.WoodSign)
                .background(FarmColors.WoodDark)
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("🌱 指尖农场", style = PicoTheme.typography.titleLarge, color = FarmColors.Gold)
            Text("键盘就是土地，打字就是耕作", style = PicoTheme.typography.bodySmall, color = FarmColors.Cream)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusPill("🪙 ${game.gold}G")
            StatusPill("${crop?.emoji.orEmpty()} ${crop?.name.orEmpty()}")
            StatusPill("${fish?.emoji.orEmpty()} ${fish?.name.orEmpty()}")
            message?.let {
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    modifier =
                        Modifier
                            .clip(FarmShapes.Message)
                            .spatialHoverEffect()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = onClearMessage,
                            )
                            .controllerHapticFeedback(interactionSource = interactionSource)
                            .background(FarmColors.GrassDark)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(it, style = PicoTheme.typography.labelSmall, color = FarmColors.Cream)
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Box(
        modifier =
            Modifier
                .clip(FarmShapes.Message)
                .background(FarmColors.WoodLight)
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text, style = PicoTheme.typography.labelMedium, color = FarmColors.Cream)
    }
}
