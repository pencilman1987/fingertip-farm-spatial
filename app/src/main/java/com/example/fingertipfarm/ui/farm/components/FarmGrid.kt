package com.example.fingertipfarm.ui.farm.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.fingertipfarm.R
import com.example.fingertipfarm.domain.model.CatalogItem
import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.FarmTheme
import com.example.fingertipfarm.domain.model.PartnerAbilityNote
import com.example.fingertipfarm.domain.model.PlotState
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType
import com.example.fingertipfarm.domain.model.ReflectionGardenSummary
import com.example.fingertipfarm.ui.navigation.FarmSection
import com.example.fingertipfarm.ui.theme.FarmColors
import com.example.fingertipfarm.ui.theme.FarmShapes
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.haptic.controllerHapticFeedback
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect

@Composable
fun FarmGrid(
    game: FarmGameState,
    message: String?,
    wpm: Int,
    lastKeyLabel: String?,
    reflection: ReflectionGardenSummary,
    partnerAbilities: List<PartnerAbilityNote>,
    sheepPatrolStep: Int,
    catFishingPulse: Int,
    sceneInteractive: Boolean,
    onClearMessage: () -> Unit,
    onToggleTheme: () -> Unit,
    onPlotClick: (String, String) -> Unit,
    onOpenSection: (FarmSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyLabels = remember { FarmDefaults.KeyboardLayout.flatten() }
    LaunchedEffect(sceneInteractive) {
        if (sceneInteractive) focusRequester.requestFocus()
    }
    Box(
        modifier =
            modifier
                .clip(FarmShapes.Panel)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (!sceneInteractive || event.type != KeyEventType.KeyDown || event.nativeKeyEvent.repeatCount > 0) {
                        false
                    } else {
                        val label = event.nativeKeyEvent.toFarmKeyLabel()
                        val index = keyLabels.indexOf(label)
                        if (index in game.plots.indices) {
                            onPlotClick(game.plots[index].id, label!!)
                            true
                        } else {
                            false
                        }
                    }
                }
                .focusable(enabled = sceneInteractive),
    ) {
        Image(
            painter = painterResource(if (game.theme == FarmTheme.DAY) R.drawable.farm_world_day else R.drawable.farm_world_night),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        FarmSceneHud(
            game = game,
            message = message,
            wpm = wpm,
            lastKeyLabel = lastKeyLabel,
            reflection = reflection,
            partnerAbilities = partnerAbilities,
            onClearMessage = onClearMessage,
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(0.57f).padding(top = 14.dp),
        )
        ThemeToggle(
            theme = game.theme,
            onClick = onToggleTheme,
            enabled = sceneInteractive,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
        )

        Row(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(0.62f).padding(top = 220.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            FarmBuilding(
                imageRes = R.drawable.farm_warehouse,
                label = "仓库",
                width = 102.dp,
                enabled = sceneInteractive,
                onClick = { onOpenSection(FarmSection.INVENTORY) },
            )
            FarmBuilding(
                imageRes = R.drawable.farm_market,
                label = "集市",
                width = 94.dp,
                enabled = sceneInteractive,
                onClick = { onOpenSection(FarmSection.MARKET) },
            )
            FarmBuilding(
                imageRes = R.drawable.farm_workshop,
                label = "工作间",
                width = 88.dp,
                enabled = sceneInteractive,
                onClick = { onOpenSection(FarmSection.WORKSHOP) },
            )
        }

        FarmKeyboard(
            game = game,
            enabled = sceneInteractive,
            onPlotClick = onPlotClick,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.78f).fillMaxHeight(0.48f).padding(bottom = 18.dp),
        )
        PartnerActors(
            game = game,
            sheepPatrolStep = sheepPatrolStep,
            catFishingPulse = catFishingPulse,
            modifier = Modifier.fillMaxSize().zIndex(2f),
        )
    }
}

@Composable
private fun PartnerActors(
    game: FarmGameState,
    sheepPatrolStep: Int,
    catFishingPulse: Int,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val idleMotion = rememberInfiniteTransition(label = "partner-idle-motion")
        val sheepBob by
            idleMotion.animateFloat(
                initialValue = -2f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(tween(360), RepeatMode.Reverse),
                label = "sheep-step-bob",
            )
        val catBreath by
            idleMotion.animateFloat(
                initialValue = 0.98f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(tween(1_400), RepeatMode.Reverse),
                label = "cat-breathe",
            )

        if ("sheep" in game.ownedPartnerIds) {
            val patrolOrderIndex = sheepPatrolStep.coerceAtLeast(0) % FarmDefaults.SheepPatrolPlotIndices.size
            val plotIndex = FarmDefaults.SheepPatrolPlotIndices[patrolOrderIndex]
            val target = partnerPlotAnchor(plotIndex, maxWidth, maxHeight)
            val sheepX by
                animateDpAsState(
                    targetValue = target.first - 48.dp,
                    animationSpec = tween(2_350, easing = FastOutSlowInEasing),
                    label = "sheep-patrol-x",
                )
            val sheepY by
                animateDpAsState(
                    targetValue = target.second - 74.dp,
                    animationSpec = tween(2_350, easing = FastOutSlowInEasing),
                    label = "sheep-patrol-y",
                )
            val rowIndex = plotRowAndColumn(plotIndex).first
            Image(
                painter = painterResource(R.drawable.partner_sheep_patrol),
                contentDescription = "小羊正在巡视农田",
                modifier =
                    Modifier
                        .offset(x = sheepX, y = sheepY)
                        .size(width = 96.dp, height = 72.dp)
                        .graphicsLayer {
                            translationY = sheepBob
                            scaleX = if (rowIndex % 2 == 0) 1f else -1f
                        },
                contentScale = ContentScale.Fit,
            )
        }

        if ("cat" in game.ownedPartnerIds) {
            val castAngle = remember { Animatable(0f) }
            LaunchedEffect(catFishingPulse) {
                if (catFishingPulse <= 0) return@LaunchedEffect
                castAngle.animateTo(-18f, tween(480))
                castAngle.animateTo(22f, tween(900, easing = FastOutSlowInEasing))
                castAngle.animateTo(0f, tween(1_620, easing = FastOutSlowInEasing))
            }
            Image(
                painter = painterResource(R.drawable.partner_cat_fishing),
                contentDescription = "猫咪守在鱼塘旁准备钓鱼",
                modifier =
                    Modifier
                        .offset(x = maxWidth * 0.79f, y = maxHeight * 0.59f)
                        .size(112.dp)
                        .graphicsLayer {
                            rotationZ = castAngle.value
                            translationY = -kotlin.math.abs(castAngle.value) * 0.32f
                            translationX = castAngle.value * 0.28f
                            scaleX = catBreath
                            scaleY = catBreath
                        },
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private fun partnerPlotAnchor(
    plotIndex: Int,
    width: Dp,
    height: Dp,
): Pair<Dp, Dp> {
    val (rowIndex, columnIndex) = plotRowAndColumn(plotIndex)
    val columnCount = FarmDefaults.KeyboardLayout[rowIndex].size
    val rowWidth =
        when (rowIndex) {
            3 -> 0.78f * 0.84f
            4 -> 0.78f * 0.48f
            else -> 0.78f
        }
    val rowStart = 0.5f - rowWidth / 2f
    val x = width * (rowStart + rowWidth * ((columnIndex + 0.5f) / columnCount))
    val y = height * (0.58f + rowIndex * 0.074f)
    return x to y
}

private fun plotRowAndColumn(plotIndex: Int): Pair<Int, Int> {
    var rowStart = 0
    FarmDefaults.KeyboardLayout.forEachIndexed { rowIndex, row ->
        if (plotIndex < rowStart + row.size) return rowIndex to (plotIndex - rowStart)
        rowStart += row.size
    }
    return 0 to 0
}

private fun android.view.KeyEvent.toFarmKeyLabel(): String? =
    when (keyCode) {
        android.view.KeyEvent.KEYCODE_SPACE -> "SPACE"
        android.view.KeyEvent.KEYCODE_ENTER, android.view.KeyEvent.KEYCODE_NUMPAD_ENTER -> "ENTER"
        in android.view.KeyEvent.KEYCODE_A..android.view.KeyEvent.KEYCODE_Z ->
            ('A'.code + keyCode - android.view.KeyEvent.KEYCODE_A).toChar().toString()
        in android.view.KeyEvent.KEYCODE_0..android.view.KeyEvent.KEYCODE_9 ->
            ('0'.code + keyCode - android.view.KeyEvent.KEYCODE_0).toChar().toString()
        android.view.KeyEvent.KEYCODE_MINUS -> "-"
        android.view.KeyEvent.KEYCODE_EQUALS -> "="
        android.view.KeyEvent.KEYCODE_LEFT_BRACKET -> "["
        android.view.KeyEvent.KEYCODE_RIGHT_BRACKET -> "]"
        android.view.KeyEvent.KEYCODE_SEMICOLON -> ";"
        android.view.KeyEvent.KEYCODE_APOSTROPHE -> "'"
        android.view.KeyEvent.KEYCODE_COMMA -> ","
        android.view.KeyEvent.KEYCODE_PERIOD -> "."
        android.view.KeyEvent.KEYCODE_SLASH -> "/"
        else -> null
    }?.takeIf { it in FarmDefaults.KeyboardLayout.flatten() }

@Composable
private fun ThemeToggle(
    theme: FarmTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            Modifier
                .clip(FarmShapes.Message)
                .semantics {
                    role = Role.Button
                    contentDescription = if (theme == FarmTheme.DAY) "切换到夜间模式" else "切换到白天模式"
                }
                .spatialHoverEffect(enabled = enabled)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                )
                .controllerHapticFeedback(interactionSource = interactionSource)
                .padding(7.dp)
                .then(modifier),
    ) {
        Text(if (theme == FarmTheme.DAY) "☀️" else "🌙", style = PicoTheme.typography.titleMedium)
    }
}

@Composable
private fun FarmSceneHud(
    game: FarmGameState,
    message: String?,
    wpm: Int,
    lastKeyLabel: String?,
    reflection: ReflectionGardenSummary,
    partnerAbilities: List<PartnerAbilityNote>,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val crop = FarmCatalog.find(game.activeCropId)
    val fish = FarmCatalog.find(game.activeFishId)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(FarmShapes.Message).background(FarmColors.Cream).padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                "${reflection.season.label} · ${game.gold}G · ${crop?.name.orEmpty()} · ${fish?.name.orEmpty()} · ${wpm} WPM · 本周 ${reflection.weeklyEntryCount} 篇",
                modifier = Modifier.align(Alignment.Center),
                style = PicoTheme.typography.labelMedium,
                color = FarmColors.WoodDark,
            )
        }
        (message ?: lastKeyLabel?.let { "刚刚照料了 $it 键" })?.let {
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
                        .background(FarmColors.Cream)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(it, style = PicoTheme.typography.labelSmall, color = FarmColors.WoodDark)
            }
        }
        val workingPartners =
            partnerAbilities
                .filter { it.partnerId == "sheep" || it.partnerId == "cat" }
                .joinToString(" · ") { if (it.partnerId == "sheep") "小羊巡逻中" else "猫咪垂钓中" }
        if (workingPartners.isNotEmpty()) {
            Box(
                modifier = Modifier.clip(FarmShapes.Message).background(FarmColors.Cream).padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(workingPartners, style = PicoTheme.typography.labelSmall, color = FarmColors.WoodDark)
            }
        }
    }
}

@Composable
private fun FarmBuilding(
    @DrawableRes imageRes: Int,
    label: String,
    width: Dp,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier =
            Modifier
                .width(width)
                .spatialHoverEffect(enabled = enabled)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                )
                .controllerHapticFeedback(interactionSource = interactionSource),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = label,
            modifier = Modifier.fillMaxWidth().height(88.dp),
            contentScale = ContentScale.Fit,
        )
        Box(
            modifier = Modifier.clip(FarmShapes.WoodSign).background(FarmColors.WoodLight).padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(label, style = PicoTheme.typography.labelSmall, color = FarmColors.Cream)
        }
    }
}

@Composable
private fun FarmKeyboard(
    game: FarmGameState,
    onPlotClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier =
            modifier
                .graphicsLayer {
                    rotationX = 28f
                    cameraDistance = 20f * density
                }
                .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Bottom),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var plotIndex = 0
        FarmDefaults.KeyboardLayout.forEachIndexed { rowIndex, labels ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(
                            when (rowIndex) {
                                3 -> 0.84f
                                4 -> 0.48f
                                else -> 1f
                            },
                        )
                        .padding(start = if (rowIndex in 1..3) (rowIndex * 6).dp else 0.dp)
                        .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                labels.forEach { label ->
                    val plot = game.plots[plotIndex]
                    FarmPlotKey(
                        plot = plot,
                        item = plot.contentId?.let(FarmCatalog::find),
                        keyLabel = label,
                        onClick = { onPlotClick(plot.id, label) },
                        modifier = Modifier.weight(if (label == "ENTER") 1.35f else 1f).fillMaxHeight(),
                        enabled = enabled,
                    )
                    plotIndex += 1
                }
            }
        }
    }
}

@Composable
private fun FarmPlotKey(
    plot: PlotState,
    item: CatalogItem?,
    keyLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val topColor: Color =
        when {
            plot.type == PlotType.POND -> FarmColors.Water
            plot.status == PlotStatus.READY -> FarmColors.Gold
            plot.status == PlotStatus.GROWING -> FarmColors.Grass
            else -> FarmColors.Soil
        }
    val depthColor = if (plot.type == PlotType.POND) FarmColors.WaterDark else if (plot.status == PlotStatus.GROWING) FarmColors.GrassDark else FarmColors.WoodDark
    val contentColor = if (plot.status == PlotStatus.GROWING || plot.status == PlotStatus.READY) FarmColors.WoodDark else FarmColors.Cream

    Box(
        modifier = Modifier.clip(FarmShapes.Plot).background(depthColor).padding(bottom = 5.dp).then(modifier),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(FarmShapes.Plot)
                    .semantics {
                        role = Role.Button
                        contentDescription = "$keyLabel 键，${item?.name ?: if (plot.type == PlotType.POND) "鱼塘" else "空农田"}"
                    }
                    .spatialHoverEffect(enabled = enabled)
                    .clickable(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick,
                    )
                    .controllerHapticFeedback(interactionSource = interactionSource)
                    .background(topColor)
                    .padding(5.dp),
        ) {
            Text(keyLabel, modifier = Modifier.align(Alignment.TopStart), style = PicoTheme.typography.labelSmall, color = contentColor)
            val cropVisual =
                when {
                    plot.type == PlotType.POND -> item?.emoji ?: "🐟"
                    plot.status == PlotStatus.GROWING -> "🌱"
                    plot.status == PlotStatus.READY -> item?.emoji.orEmpty()
                    else -> ""
                }
            if (cropVisual.isNotEmpty()) {
                Text(
                    cropVisual,
                    modifier = Modifier.align(Alignment.Center),
                    style = PicoTheme.typography.titleMedium,
                    color = contentColor,
                )
            }
            val statusText: String? =
                when (plot.status) {
                    PlotStatus.EMPTY -> if (plot.type == PlotType.POND) "投苗" else null
                    PlotStatus.GROWING -> "${plot.growth}/${item?.growthNeeded ?: 0}"
                    PlotStatus.READY -> if (plot.type == PlotType.POND) "×${plot.count} 收获" else "收获"
                }
            statusText?.let {
                Text(it, modifier = Modifier.align(Alignment.BottomEnd), style = PicoTheme.typography.labelSmall, color = contentColor)
            }
        }
    }
}
