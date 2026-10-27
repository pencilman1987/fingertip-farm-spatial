package com.example.fingertipfarm.ui.farm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text

@Composable
fun FarmIllustratedHeader(
    title: String,
    subtitle: String,
    @DrawableRes illustrationRes: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(FarmShapes.Card)
                .background(FarmColors.Cream)
                .padding(horizontal = 18.dp)
                .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = PicoTheme.typography.titleLarge, color = FarmColors.WoodDark)
            Text(subtitle, style = PicoTheme.typography.bodyMedium, color = FarmColors.WoodLight)
        }
        Image(
            painter = painterResource(illustrationRes),
            contentDescription = null,
            modifier = Modifier.size(94.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
