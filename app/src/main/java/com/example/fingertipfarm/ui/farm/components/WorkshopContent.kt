package com.example.fingertipfarm.ui.farm.components

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.fingertipfarm.R
import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.JournalEntry
import com.example.fingertipfarm.domain.model.JournalBorderStyle
import com.example.fingertipfarm.domain.model.JournalMood
import com.example.fingertipfarm.domain.model.JournalPageTheme
import com.example.fingertipfarm.domain.model.MemoryCard
import com.example.fingertipfarm.domain.model.PartnerAbilityNote
import com.example.fingertipfarm.domain.model.ReflectionGardenSummary
import com.example.fingertipfarm.domain.model.PlotState
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType
import com.example.fingertipfarm.domain.model.WorkshopInputMode
import com.example.fingertipfarm.domain.model.WorkshopPinyin
import com.example.fingertipfarm.ui.farm.WorkshopMode
import com.example.fingertipfarm.ui.theme.FarmColors
import com.example.fingertipfarm.ui.theme.FarmShapes
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.LinearProgressIndicator
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.SegmentControl
import com.pico.spatial.ui.design.SegmentItem
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.design.TextField
import com.pico.spatial.ui.design.ToggleableChip
import com.pico.spatial.ui.foundation.haptic.controllerHapticFeedback
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun WorkshopContent(
    game: FarmGameState,
    message: String?,
    workshopMode: WorkshopMode,
    gestureKeyboardExpanded: Boolean,
    journalDetailsExpanded: Boolean,
    scrapbookToolsExpanded: Boolean,
    pinyinBuffer: String,
    todayDateKey: String,
    reflection: ReflectionGardenSummary,
    partnerAbilities: List<PartnerAbilityNote>,
    onToggleTimer: () -> Unit,
    onCreateJournal: () -> Unit,
    onSelectJournal: (String) -> Unit,
    onSelectMode: (WorkshopMode) -> Unit,
    onTitleChange: (String, Boolean) -> Unit,
    onTextChange: (String, Boolean) -> Unit,
    onKeyPress: (String, String) -> Unit,
    onSelectCandidate: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onBackspace: () -> Unit,
    onCompleteJournal: () -> Unit,
    onToggleGestureKeyboard: () -> Unit,
    onToggleJournalDetails: () -> Unit,
    onToggleScrapbookTools: () -> Unit,
    onSetMood: (JournalMood) -> Unit,
    onToggleTag: (String) -> Unit,
    onToggleSticker: (String) -> Unit,
    onSetBorder: (JournalBorderStyle) -> Unit,
    onSetPageTheme: (JournalPageTheme) -> Unit,
    onSetLocalImage: (String?) -> Unit,
    onBackToFarm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeEntry = game.journalEntries.firstOrNull { it.id == game.activeJournalEntryId }
    val context = LocalContext.current
    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onSetLocalImage(uri.toString())
        }
    Column(modifier = Modifier.fillMaxHeight().then(modifier), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WorkshopHeader(
            game = game,
            activeEntry = activeEntry,
            onToggleTimer = onToggleTimer,
            onCreateJournal = onCreateJournal,
            onBackToFarm = onBackToFarm,
        )
        WorkshopModeSwitcher(
            selected = workshopMode,
            journalCount = game.journalEntries.size,
            memoryCount = game.memoryCards.size,
            onSelectMode = onSelectMode,
        )
        val contextualPartner =
            partnerAbilities.firstOrNull {
                when (workshopMode) {
                    WorkshopMode.WRITE -> it.partnerId == "pig" || it.partnerId == "llama"
                    WorkshopMode.ARCHIVE, WorkshopMode.MEMORIES -> false
                }
            }
        contextualPartner?.let { PartnerAbilityStrip(it) }
        when (workshopMode) {
            WorkshopMode.WRITE ->
                if (activeEntry == null) {
                    EmptyJournalState(onCreateJournal = onCreateJournal, modifier = Modifier.fillMaxSize())
                } else {
                    JournalWritingContent(
                        game = game,
                        entry = activeEntry,
                        message = message,
                        gestureKeyboardExpanded = gestureKeyboardExpanded,
                        journalDetailsExpanded = journalDetailsExpanded,
                        scrapbookToolsExpanded = scrapbookToolsExpanded,
                        pinyinBuffer = pinyinBuffer,
                        onTitleChange = onTitleChange,
                        onTextChange = onTextChange,
                        onKeyPress = onKeyPress,
                        onSelectCandidate = onSelectCandidate,
                        onToggleInputMode = onToggleInputMode,
                        onBackspace = onBackspace,
                        onCompleteJournal = onCompleteJournal,
                        onToggleGestureKeyboard = onToggleGestureKeyboard,
                        onToggleJournalDetails = onToggleJournalDetails,
                        onToggleScrapbookTools = onToggleScrapbookTools,
                        onSetMood = onSetMood,
                        onToggleTag = onToggleTag,
                        onToggleSticker = onToggleSticker,
                        onSetBorder = onSetBorder,
                        onSetPageTheme = onSetPageTheme,
                        onPickLocalImage = { imagePicker.launch(arrayOf("image/*")) },
                        onRemoveLocalImage = { onSetLocalImage(null) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            WorkshopMode.ARCHIVE ->
                CalendarGardenContent(
                    game = game,
                    todayDateKey = todayDateKey,
                    reflection = reflection,
                    onSelectJournal = onSelectJournal,
                    onCreateJournal = onCreateJournal,
                    modifier = Modifier.fillMaxSize(),
                )
            WorkshopMode.MEMORIES ->
                MemoryGalleryContent(
                    game = game,
                    onSelectJournal = onSelectJournal,
                    modifier = Modifier.fillMaxSize(),
                )
        }
    }
}

@Composable
private fun WorkshopHeader(
    game: FarmGameState,
    activeEntry: JournalEntry?,
    onToggleTimer: () -> Unit,
    onCreateJournal: () -> Unit,
    onBackToFarm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(54.dp).then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.farm_workshop),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit,
            )
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text("记忆工坊", style = PicoTheme.typography.titleLarge)
                Text(
                    activeEntry?.let { "${it.dateKey} · 写下的生活会长成作物" } ?: "写下一篇日记，种下一株记忆作物",
                    style = PicoTheme.typography.bodyMedium,
                    color = PicoTheme.colorScheme.labelSecondary,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBackToFarm) { Text("返回农场") }
            Button(onClick = onCreateJournal) { Text("新日记") }
            Button(onClick = onToggleTimer) {
                Text(if (game.timerRunning) "暂停 ${formatTime(game.timerSeconds)}" else "专注 ${formatTime(game.timerSeconds)}")
            }
        }
    }
}

@Composable
private fun WorkshopModeSwitcher(
    selected: WorkshopMode,
    journalCount: Int,
    memoryCount: Int,
    onSelectMode: (WorkshopMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentControl(modifier = Modifier.fillMaxWidth().then(modifier)) {
        SegmentItem(
            selected = selected == WorkshopMode.WRITE,
            onClick = { onSelectMode(WorkshopMode.WRITE) },
            title = { Text("写作") },
        )
        SegmentItem(
            selected = selected == WorkshopMode.ARCHIVE,
            onClick = { onSelectMode(WorkshopMode.ARCHIVE) },
            title = { Text("日历 · $journalCount") },
        )
        SegmentItem(
            selected = selected == WorkshopMode.MEMORIES,
            onClick = { onSelectMode(WorkshopMode.MEMORIES) },
            title = { Text("记忆卡 · $memoryCount") },
        )
    }
}

@Composable
private fun JournalWritingContent(
    game: FarmGameState,
    entry: JournalEntry,
    message: String?,
    gestureKeyboardExpanded: Boolean,
    journalDetailsExpanded: Boolean,
    scrapbookToolsExpanded: Boolean,
    pinyinBuffer: String,
    onTitleChange: (String, Boolean) -> Unit,
    onTextChange: (String, Boolean) -> Unit,
    onKeyPress: (String, String) -> Unit,
    onSelectCandidate: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onBackspace: () -> Unit,
    onCompleteJournal: () -> Unit,
    onToggleGestureKeyboard: () -> Unit,
    onToggleJournalDetails: () -> Unit,
    onToggleScrapbookTools: () -> Unit,
    onSetMood: (JournalMood) -> Unit,
    onToggleTag: (String) -> Unit,
    onToggleSticker: (String) -> Unit,
    onSetBorder: (JournalBorderStyle) -> Unit,
    onSetPageTheme: (JournalPageTheme) -> Unit,
    onPickLocalImage: () -> Unit,
    onRemoveLocalImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var titleValue by remember(entry.id) { mutableStateOf(TextFieldValue(entry.title)) }
    var editorValue by remember(entry.id) { mutableStateOf(TextFieldValue(entry.body, selection = TextRange(entry.body.length))) }
    LaunchedEffect(entry.title) {
        if (titleValue.composition == null && titleValue.text != entry.title) {
            titleValue = TextFieldValue(entry.title, selection = TextRange(entry.title.length))
        }
    }
    LaunchedEffect(entry.body) {
        if (editorValue.composition == null && editorValue.text != entry.body) {
            editorValue = TextFieldValue(entry.body, selection = TextRange(entry.body.length))
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinkedCropStatus(game = game, entry = entry, message = message)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                TextField(
                    value = titleValue,
                    onValueChange = { value ->
                        titleValue = value
                        onTitleChange(value.text, value.composition == null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("给今天的记录起个名字") },
                    singleLine = true,
                    clearable = false,
                )
            }
            Button(onClick = onCompleteJournal, enabled = entry.completedAtMillis == null && entry.body.isNotBlank()) {
                Text(if (entry.completedAtMillis == null) "完成记录" else "已完成")
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onToggleInputMode) {
                Text(if (game.workshopInputMode == WorkshopInputMode.CHINESE) "中文 / EN" else "EN / 中文")
            }
            Button(onClick = onToggleJournalDetails) { Text(if (journalDetailsExpanded) "收起心情" else "心情·标签") }
            Button(onClick = onToggleScrapbookTools) { Text(if (scrapbookToolsExpanded) "收起手账" else "手账装饰") }
            Button(onClick = onToggleGestureKeyboard) { Text(if (gestureKeyboardExpanded) "收起键盘" else "空间键盘") }
        }
        if (journalDetailsExpanded) {
            ReflectionControls(entry = entry, onSetMood = onSetMood, onToggleTag = onToggleTag)
        }
        if (scrapbookToolsExpanded) {
            ScrapbookControls(
                entry = entry,
                llamaOwned = "llama" in game.ownedPartnerIds,
                onToggleSticker = onToggleSticker,
                onSetBorder = onSetBorder,
                onSetPageTheme = onSetPageTheme,
                onPickLocalImage = onPickLocalImage,
                onRemoveLocalImage = onRemoveLocalImage,
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(FarmShapes.Card)
                    .background(PicoTheme.colorScheme.fillSecondary)
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("日记正文", style = PicoTheme.typography.titleSmall, color = PicoTheme.colorScheme.labelPrimary)
                Text(
                    "自动保存 · ${entry.body.length} 字 / ${entry.body.lineSequence().count()} 行",
                    style = PicoTheme.typography.labelMedium,
                    color = PicoTheme.colorScheme.labelSecondary,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(FarmShapes.Card)
                        .border(2.dp, entry.borderColor(), FarmShapes.Card)
                        .background(entry.pageColor())
                        .padding(8.dp),
            ) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (entry.stickerIds.isNotEmpty()) {
                            Text(entry.stickerIds.joinToString("  ") { StickerLabels[it] ?: it }, style = PicoTheme.typography.titleMedium)
                        }
                        TextField(
                            value = editorValue,
                            onValueChange = { value ->
                                editorValue = value
                                onTextChange(value.text, value.composition == null)
                            },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = { Text("> ${entry.dailyPrompt.ifBlank { "今天发生了什么？此刻有什么感受？" }}") },
                            singleLine = false,
                            minLines = 4,
                            maxLines = 12,
                            clearable = false,
                        )
                    }
                    entry.localImageUri?.let { LocalJournalImage(uri = it, modifier = Modifier.fillMaxHeight().weight(0.34f)) }
                }
            }
        }
        if (game.workshopInputMode == WorkshopInputMode.CHINESE && gestureKeyboardExpanded) {
            ChineseCandidateBar(pinyinBuffer = pinyinBuffer, onSelectCandidate = onSelectCandidate)
        }
        if (gestureKeyboardExpanded) {
            Button(onClick = onBackspace) { Text("退格") }
            GestureKeyboard(game = game, onKeyPress = onKeyPress)
        }
    }
}

@Composable
private fun ReflectionControls(
    entry: JournalEntry,
    onSetMood: (JournalMood) -> Unit,
    onToggleTag: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(FarmShapes.Card).background(PicoTheme.colorScheme.fillSecondary).padding(10.dp).then(modifier),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("此刻", style = PicoTheme.typography.labelMedium)
            JournalMood.entries.forEach { mood ->
                ToggleableChip(label = { Text("${mood.symbol} ${mood.label}") }, isToggleOn = entry.mood == mood, onClick = { onSetMood(mood) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("标签", style = PicoTheme.typography.labelMedium)
            JournalTags.forEach { tag ->
                ToggleableChip(label = { Text(tag) }, isToggleOn = tag in entry.tags, onClick = { onToggleTag(tag) })
            }
            Text("最多 3 个", style = PicoTheme.typography.labelSmall, color = PicoTheme.colorScheme.labelTertiary)
        }
    }
}

@Composable
private fun ScrapbookControls(
    entry: JournalEntry,
    llamaOwned: Boolean,
    onToggleSticker: (String) -> Unit,
    onSetBorder: (JournalBorderStyle) -> Unit,
    onSetPageTheme: (JournalPageTheme) -> Unit,
    onPickLocalImage: () -> Unit,
    onRemoveLocalImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stickers = if (llamaOwned) StickerLabels.keys else StickerLabels.keys.take(4)
    Column(
        modifier = Modifier.fillMaxWidth().clip(FarmShapes.Card).background(PicoTheme.colorScheme.fillSecondary).padding(10.dp).then(modifier),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("贴纸", style = PicoTheme.typography.labelMedium)
            stickers.forEach { id -> ToggleableChip(label = { Text(StickerLabels.getValue(id)) }, isToggleOn = id in entry.stickerIds, onClick = { onToggleSticker(id) }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("边框", style = PicoTheme.typography.labelMedium)
            JournalBorderStyle.entries.forEach { style -> ToggleableChip(label = { Text(style.label) }, isToggleOn = entry.borderStyle == style, onClick = { onSetBorder(style) }) }
            Text("主题", style = PicoTheme.typography.labelMedium)
            JournalPageTheme.entries.forEach { theme -> ToggleableChip(label = { Text(theme.label) }, isToggleOn = entry.pageTheme == theme, onClick = { onSetPageTheme(theme) }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onPickLocalImage) { Text(if (entry.localImageUri == null) "添加本地图片" else "更换本地图片") }
            if (entry.localImageUri != null) Button(onClick = onRemoveLocalImage) { Text("移除图片") }
            Text(if (llamaOwned) "羊驼带来了季节贴纸" else "招募羊驼可看到更多季节贴纸", style = PicoTheme.typography.labelSmall, color = PicoTheme.colorScheme.labelSecondary)
        }
    }
}

@Composable
private fun LocalJournalImage(
    uri: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(uri))?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                }.getOrNull()
            }
    }
    Box(modifier = modifier.clip(FarmShapes.Card).background(PicoTheme.colorScheme.fillTertiary), contentAlignment = Alignment.Center) {
        if (bitmap == null) {
            Text("图片读取中", style = PicoTheme.typography.labelSmall)
        } else {
            Image(bitmap = bitmap!!, contentDescription = "日记中的本地图片", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun PartnerAbilityStrip(
    note: PartnerAbilityNote,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(FarmShapes.Message).background(PicoTheme.colorScheme.fillTertiary).padding(horizontal = 12.dp, vertical = 6.dp).then(modifier),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("${note.symbol} ${note.partnerName}", style = PicoTheme.typography.labelMedium)
        Text(note.message, style = PicoTheme.typography.labelSmall, color = PicoTheme.colorScheme.labelSecondary)
    }
}

@Composable
private fun LinkedCropStatus(
    game: FarmGameState,
    entry: JournalEntry,
    message: String?,
    modifier: Modifier = Modifier,
) {
    val plot = game.plots.firstOrNull { it.id == entry.linkedPlotId }
    val crop = FarmCatalog.find(entry.cropId)
    val memoryHarvested = game.memoryCards.any { it.journalEntryId == entry.id }
    val progress =
        when {
            memoryHarvested -> 1f
            crop == null || crop.growthNeeded == 0 -> 0f
            else -> (plot?.growth ?: 0).toFloat() / crop.growthNeeded
        }.coerceIn(0f, 1f)
    val status =
        when {
            memoryHarvested -> "已收获为记忆卡，可继续补写"
            plot?.status == PlotStatus.READY && entry.completedAtMillis != null -> "已经成熟，回农场收获记忆卡"
            plot?.status == PlotStatus.READY -> "作物已成熟，完成日记后即可收获"
            entry.completedAtMillis != null -> "日记已完成，继续补写仍会自动保存"
            else -> "每写满 20 个有效字符，记忆作物成长 1 点"
        }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(FarmShapes.WoodSign)
                .background(FarmColors.Cream)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1.15f)) {
            Text("${crop?.name ?: "记忆作物"} · ${entry.dateKey}", style = PicoTheme.typography.titleMedium, color = FarmColors.WoodDark)
            Text(status, style = PicoTheme.typography.bodyMedium, color = FarmColors.WoodLight)
            Text("今日问题：${entry.dailyPrompt.ifBlank { "想写什么都可以。" }}", style = PicoTheme.typography.labelMedium, color = FarmColors.WoodDark)
        }
        Column(modifier = Modifier.weight(0.65f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp))
            Text(
                if (memoryHarvested) "记忆已经被收藏" else "成长 ${plot?.growth ?: 0}/${crop?.growthNeeded ?: 0}",
                style = PicoTheme.typography.labelSmall,
                color = FarmColors.WoodDark,
            )
        }
        message?.let { Text(it, modifier = Modifier.weight(0.65f), style = PicoTheme.typography.labelMedium, color = FarmColors.WoodDark) }
    }
}

@Composable
private fun CalendarGardenContent(
    game: FarmGameState,
    todayDateKey: String,
    reflection: ReflectionGardenSummary,
    onSelectJournal: (String) -> Unit,
    onCreateJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entriesByDate = game.journalEntries.sortedByDescending { it.updatedAtMillis }.groupBy { it.dateKey }
    val today = runCatching { LocalDate.parse(todayDateKey) }.getOrElse { LocalDate.now() }
    val month = YearMonth.from(today)
    val leadingEmptyCells = month.atDay(1).dayOfWeek.value - 1
    val calendarCells = List<LocalDate?>(leadingEmptyCells) { null } + (1..month.lengthOfMonth()).map(month::atDay)
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("${month.year} 年 ${month.monthValue} 月 · 日历花园", style = PicoTheme.typography.titleLarge)
                Text("本周 ${reflection.weeklyEntryCount} 篇 · 本季 ${reflection.seasonEntryCount} 份成长 · 空白日期不会带来任何惩罚。", style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
            }
            Button(onClick = onCreateJournal) { Text("写今天") }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WeekdayLabels.forEach { label -> Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(label, style = PicoTheme.typography.labelSmall) } }
        }
        calendarCells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (week + List(7 - week.size) { null }).forEach { date ->
                    if (date == null) {
                        Spacer(modifier = Modifier.weight(1f).height(42.dp))
                    } else {
                        val entries = entriesByDate[date.toString()].orEmpty()
                        CalendarDayCell(
                            date = date,
                            entryCount = entries.size,
                            seasonMark = reflection.season.gardenSymbol,
                            isToday = date == today,
                            onClick = { entries.firstOrNull()?.let { onSelectJournal(it.id) } },
                            modifier = Modifier.weight(1f).height(42.dp),
                        )
                    }
                }
            }
        }
        WeeklyReviewCard(reflection = reflection)
        Text("按日期回看", style = PicoTheme.typography.titleMedium)
        if (entriesByDate.isEmpty()) {
            Text("这个月还很安静。想写的时候，任何一天都可以成为开始。", style = PicoTheme.typography.bodyMedium, color = PicoTheme.colorScheme.labelSecondary)
        }
        entriesByDate.forEach { (date, entries) ->
            Text(date, style = PicoTheme.typography.titleMedium, color = FarmColors.WoodDark)
            entries.forEach { entry ->
                JournalEntryCard(
                    entry = entry,
                    game = game,
                    onClick = { onSelectJournal(entry.id) },
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    entryCount: Int,
    seasonMark: String,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val enabled = entryCount > 0
    Column(
        modifier =
            Modifier
                .clip(FarmShapes.Card)
                .semantics { contentDescription = "${date.dayOfMonth} 日，${entryCount} 篇记录" }
                .spatialHoverEffect(enabled = enabled)
                .clickable(enabled = enabled, interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
                .controllerHapticFeedback(interactionSource = interactionSource)
                .background(if (entryCount > 0) FarmColors.Cream else PicoTheme.colorScheme.fillSecondary)
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(if (entryCount > 0) "${date.dayOfMonth} · $seasonMark" else date.dayOfMonth.toString(), style = PicoTheme.typography.labelMedium, color = if (entryCount > 0) FarmColors.WoodDark else PicoTheme.colorScheme.labelSecondary)
        if (isToday) Text("今天", style = PicoTheme.typography.labelSmall, color = PicoTheme.colorScheme.interaction)
    }
}

@Composable
private fun WeeklyReviewCard(
    reflection: ReflectionGardenSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(FarmShapes.WoodSign).background(FarmColors.Cream).padding(14.dp).then(modifier),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text("每周回顾 · ${reflection.season.label}", style = PicoTheme.typography.titleMedium, color = FarmColors.WoodDark)
        Text("本周 ${reflection.weeklyEntryCount} 篇 · 完成 ${reflection.weeklyCompletedCount} 篇 · 记忆卡 ${reflection.weeklyMemoryCount} 张 · ${reflection.weeklyCharacterCount} 个字", style = PicoTheme.typography.bodySmall, color = FarmColors.WoodLight)
        Text("本季已经长出 ${reflection.seasonEntryCount} 份记录，收获 ${reflection.seasonMemoryCount} 张记忆卡。", style = PicoTheme.typography.bodySmall, color = FarmColors.WoodDark)
        Text(reflection.gentleMessage, style = PicoTheme.typography.labelMedium, color = FarmColors.WoodDark)
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    game: FarmGameState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val crop = FarmCatalog.find(entry.cropId)
    val memoryHarvested = game.memoryCards.any { it.journalEntryId == entry.id }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(FarmShapes.Card)
                .spatialHoverEffect(enabled = enabled)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                )
                .controllerHapticFeedback(interactionSource = interactionSource)
                .background(PicoTheme.colorScheme.fillSecondary)
                .padding(14.dp)
                .then(modifier),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(entry.title.ifBlank { "未命名日记" }, style = PicoTheme.typography.titleMedium)
            Text("${crop?.emoji ?: "🌱"} ${if (memoryHarvested) "已收获" else if (entry.completedAtMillis != null) "已完成" else "书写中"}", style = PicoTheme.typography.labelMedium)
        }
        Text(entry.body.previewText(), style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
        Text("${entry.body.length} 字 · 自动保存", style = PicoTheme.typography.labelSmall, color = PicoTheme.colorScheme.labelTertiary)
    }
}

@Composable
private fun MemoryGalleryContent(
    game: FarmGameState,
    onSelectJournal: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = game.memoryCards.sortedByDescending { it.harvestedAtMillis }
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("记忆卡", style = PicoTheme.typography.titleLarge)
        Text("每张卡都来自一篇完成并收获的日记。选择卡片可重新打开原文。", style = PicoTheme.typography.bodySmall, color = PicoTheme.colorScheme.labelSecondary)
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(FarmShapes.Card).background(PicoTheme.colorScheme.fillSecondary).padding(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("还没有记忆卡", style = PicoTheme.typography.titleMedium)
                    Text("完成一篇日记，等它的作物成熟后回农场收获。", style = PicoTheme.typography.bodyMedium, color = PicoTheme.colorScheme.labelSecondary)
                }
            }
        } else {
            cards.forEach { card ->
                val entry = game.journalEntries.firstOrNull { it.id == card.journalEntryId }
                if (entry != null) {
                    MemoryCardItem(card = card, entry = entry, onClick = { onSelectJournal(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun MemoryCardItem(
    card: MemoryCard,
    entry: JournalEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val crop = FarmCatalog.find(entry.cropId)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(FarmShapes.WoodSign)
                .spatialHoverEffect(enabled = enabled)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                )
                .controllerHapticFeedback(interactionSource = interactionSource)
                .background(FarmColors.Cream)
                .padding(16.dp)
                .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(crop?.emoji ?: "🌱", style = PicoTheme.typography.displaySmall)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(entry.title.ifBlank { "未命名日记" }, style = PicoTheme.typography.titleMedium, color = FarmColors.WoodDark)
            Text(entry.body.previewText(), style = PicoTheme.typography.bodySmall, color = FarmColors.WoodLight)
            Text("${entry.dateKey} · 已收获 · 点击重新阅读", style = PicoTheme.typography.labelSmall, color = FarmColors.WoodDark)
        }
        Text("记忆 #${card.id.substringAfterLast('_')}", style = PicoTheme.typography.labelMedium, color = FarmColors.WoodDark)
    }
}

@Composable
private fun EmptyJournalState(
    onCreateJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("种下第一篇日记", style = PicoTheme.typography.titleLarge)
            Text("日记会自动保存，并和一株真实农作物一起成长。", style = PicoTheme.typography.bodyMedium, color = PicoTheme.colorScheme.labelSecondary)
            Button(onClick = onCreateJournal) { Text("新建日记") }
        }
    }
}

@Composable
private fun ChineseCandidateBar(
    pinyinBuffer: String,
    onSelectCandidate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val candidates = WorkshopPinyin.candidates(pinyinBuffer)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(FarmShapes.Card)
                .background(FarmColors.Cream)
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (pinyinBuffer.isEmpty()) "拼音：点击下方字母" else "拼音：$pinyinBuffer",
            modifier = Modifier.weight(1f),
            style = PicoTheme.typography.bodySmall,
            color = FarmColors.WoodDark,
        )
        candidates.take(3).forEach { candidate -> Button(onClick = { onSelectCandidate(candidate) }) { Text(candidate) } }
        if (pinyinBuffer.isNotEmpty()) Button(onClick = { onSelectCandidate(pinyinBuffer) }) { Text("直接上屏") }
    }
}

@Composable
private fun GestureKeyboard(
    game: FarmGameState,
    onKeyPress: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var plotIndex = 0
    Column(
        modifier = Modifier.fillMaxWidth().then(modifier),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FarmDefaults.KeyboardLayout.forEachIndexed { rowIndex, keys ->
            Row(
                modifier =
                    Modifier.fillMaxWidth(
                        when (rowIndex) {
                            3 -> 0.84f
                            4 -> 0.48f
                            else -> 1f
                        },
                    ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                keys.forEach { key ->
                    val plot = game.plots[plotIndex]
                    GestureFarmKey(
                        label = key,
                        plot = plot,
                        onClick = { onKeyPress(plot.id, key) },
                        modifier = Modifier.weight(if (key == "ENTER") 1.35f else 1f),
                    )
                    plotIndex += 1
                }
            }
        }
    }
}

@Composable
private fun GestureFarmKey(
    label: String,
    plot: PlotState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val item = plot.contentId?.let(FarmCatalog::find)
    val topColor =
        when {
            plot.type == PlotType.POND -> FarmColors.Water
            plot.status == PlotStatus.READY -> FarmColors.Gold
            plot.status == PlotStatus.GROWING -> FarmColors.Grass
            else -> FarmColors.Soil
        }
    val depthColor =
        when {
            plot.type == PlotType.POND -> FarmColors.WaterDark
            plot.status == PlotStatus.GROWING -> FarmColors.GrassDark
            else -> FarmColors.WoodDark
        }
    val contentColor = if (plot.status == PlotStatus.EMPTY && plot.type != PlotType.POND) FarmColors.Cream else FarmColors.WoodDark
    Box(
        modifier =
            Modifier
                .height(30.dp)
                .clip(FarmShapes.Plot)
                .background(depthColor)
                .padding(bottom = 5.dp)
                .then(modifier),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(FarmShapes.Plot)
                    .semantics {
                        role = Role.Button
                        contentDescription = "手势键盘 $label 键，${item?.name ?: if (plot.type == PlotType.POND) "鱼塘" else "空农田"}"
                    }
                    .spatialHoverEffect(enabled = enabled)
                    .clickable(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick,
                    )
                    .controllerHapticFeedback(interactionSource = interactionSource)
                    .background(topColor),
            contentAlignment = Alignment.Center,
        ) {
            val cropMark =
                when {
                    plot.status == PlotStatus.READY -> item?.emoji.orEmpty()
                    plot.status == PlotStatus.GROWING -> "🌱"
                    plot.type == PlotType.POND -> "🐟"
                    else -> ""
                }
            Text(if (cropMark.isEmpty()) label else "$label $cropMark", style = PicoTheme.typography.labelSmall, color = contentColor)
        }
    }
}

private fun String.previewText(): String =
    trim().replace('\n', ' ').ifBlank { "还没有正文，点击继续书写。" }.let { if (it.length > 90) it.take(90) + "…" else it }

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun JournalEntry.pageColor(): Color =
    when (pageTheme) {
        JournalPageTheme.CREAM -> FarmColors.Cream
        JournalPageTheme.FOREST -> FarmColors.Grass
        JournalPageTheme.SKY -> FarmColors.Water
        JournalPageTheme.NIGHT -> FarmColors.CrtScreen
    }

private fun JournalEntry.borderColor(): Color =
    when (borderStyle) {
        JournalBorderStyle.CLASSIC -> FarmColors.WoodLight
        JournalBorderStyle.LEAF -> FarmColors.GrassDark
        JournalBorderStyle.SKY -> FarmColors.WaterDark
        JournalBorderStyle.NIGHT -> FarmColors.CrtGreen
    }

private val JournalTags = listOf("生活", "工作", "学习", "灵感", "感恩", "休息")
private val WeekdayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
private val StickerLabels =
    linkedMapOf(
        "leaf" to "叶",
        "sun" to "晴",
        "star" to "星",
        "heart" to "心",
        "rain" to "雨",
        "seed" to "籽",
    )
