package com.example.fingertipfarm.ui.farm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fingertipfarm.ui.theme.FarmColors
import com.example.fingertipfarm.ui.theme.FarmShapes
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text

@Composable
fun FarmRoomOverlay(
    title: String,
    subtitle: String,
    gold: Int,
    @DrawableRes illustrationRes: Int,
    message: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(FarmColors.SceneScrim).then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(0.78f)
                    .fillMaxHeight(0.82f)
                    .clip(FarmShapes.Panel)
                    .border(8.dp, FarmColors.ModalBorder, FarmShapes.Panel)
                    .background(FarmColors.Cream)
                    .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(illustrationRes),
                        contentDescription = null,
                        modifier = Modifier.size(78.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(title, style = PicoTheme.typography.titleLarge, color = FarmColors.WoodDark)
                        Text(subtitle, style = PicoTheme.typography.bodySmall, color = FarmColors.WoodLight)
                    }
                }
                Box(
                    modifier =
                        Modifier
                            .clip(FarmShapes.Message)
                            .background(FarmColors.Gold)
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                ) {
                    Text("🪙 您的金币：${gold}G", style = PicoTheme.typography.labelLarge, color = FarmColors.WoodDark)
                }
            }
            message?.let {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(FarmShapes.Message)
                            .background(FarmColors.Grass)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Text(it, style = PicoTheme.typography.labelMedium, color = FarmColors.WoodDark)
                }
            }
            Column(modifier = Modifier.fillMaxWidth().weight(1f), content = content)
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text("关闭房间 · 返回农场")
            }
        }
    }
}
