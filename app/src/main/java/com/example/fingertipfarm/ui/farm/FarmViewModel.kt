package com.example.fingertipfarm.ui.farm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fingertipfarm.data.repository.FarmRepository
import com.example.fingertipfarm.data.repository.SharedPreferencesFarmRepository
import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.FarmTheme
import com.example.fingertipfarm.domain.model.JournalEntry
import com.example.fingertipfarm.domain.model.JournalBorderStyle
import com.example.fingertipfarm.domain.model.JournalMood
import com.example.fingertipfarm.domain.model.JournalPageTheme
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.WorkshopInputMode
import com.example.fingertipfarm.domain.model.WorkshopPinyin
import com.example.fingertipfarm.domain.model.SpatialExperienceMode
import com.example.fingertipfarm.domain.usecase.ApplyJournalWritingRewardUseCase
import com.example.fingertipfarm.domain.usecase.ApplyTypingGrowthUseCase
import com.example.fingertipfarm.domain.usecase.CompleteJournalEntryUseCase
import com.example.fingertipfarm.domain.usecase.CreateJournalEntryUseCase
import com.example.fingertipfarm.domain.usecase.CreateMemoryCardUseCase
import com.example.fingertipfarm.domain.usecase.DailyPromptUseCase
import com.example.fingertipfarm.domain.usecase.InteractWithPlotUseCase
import com.example.fingertipfarm.domain.usecase.PlotInteractionResult
import com.example.fingertipfarm.domain.usecase.PurchaseItemUseCase
import com.example.fingertipfarm.domain.usecase.PartnerAbilityUseCase
import com.example.fingertipfarm.domain.usecase.PartnerAutomationUseCase
import com.example.fingertipfarm.domain.usecase.ReflectionGardenUseCase
import com.example.fingertipfarm.domain.usecase.ResolveSpatialAmbienceUseCase
import com.example.fingertipfarm.domain.usecase.SelectSpeciesUseCase
import com.example.fingertipfarm.ui.navigation.FarmSection
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FarmViewModel(
    private val repository: FarmRepository,
    private val interactWithPlot: InteractWithPlotUseCase = InteractWithPlotUseCase(),
    private val purchaseItem: PurchaseItemUseCase = PurchaseItemUseCase(),
    private val selectSpecies: SelectSpeciesUseCase = SelectSpeciesUseCase(),
    private val applyTypingGrowth: ApplyTypingGrowthUseCase = ApplyTypingGrowthUseCase(interactWithPlot),
    private val createJournalEntry: CreateJournalEntryUseCase = CreateJournalEntryUseCase(interactWithPlot),
    private val applyJournalWritingReward: ApplyJournalWritingRewardUseCase = ApplyJournalWritingRewardUseCase(interactWithPlot),
    private val completeJournalEntry: CompleteJournalEntryUseCase = CompleteJournalEntryUseCase(interactWithPlot),
    private val createMemoryCard: CreateMemoryCardUseCase = CreateMemoryCardUseCase(),
    private val dailyPrompt: DailyPromptUseCase = DailyPromptUseCase(),
    private val reflectionGarden: ReflectionGardenUseCase = ReflectionGardenUseCase(),
    private val partnerAbility: PartnerAbilityUseCase = PartnerAbilityUseCase(),
    private val partnerAutomation: PartnerAutomationUseCase = PartnerAutomationUseCase(interactWithPlot),
    private val resolveSpatialAmbience: ResolveSpatialAmbienceUseCase = ResolveSpatialAmbienceUseCase(),
    private val timerDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val timeProvider: () -> Long = System::currentTimeMillis,
    private val dateKeyProvider: (Long) -> String = { millis ->
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
    },
) : ViewModel() {
    private val loadedGame = repository.load()
    private val _state = MutableStateFlow(FarmUiState(game = loadedGame).withDerivedGame(loadedGame, null))
    val state: StateFlow<FarmUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var wpmJob: Job? = null
    private var partnerJob: Job? = null
    private val inputPressTimes = ArrayDeque<Long>()

    init {
        startWpmTicker()
        startPartnerAutomation()
    }

    fun onEvent(event: FarmEvent) {
        when (event) {
            is FarmEvent.SelectSpatialMode -> selectSpatialMode(event.mode)
            is FarmEvent.SelectSection -> selectSection(event.section)
            is FarmEvent.TapPlot -> handlePlotTap(event)
            is FarmEvent.SelectSpecies -> handleSpeciesSelection(event.itemId)
            is FarmEvent.Purchase -> handlePurchase(event.item)
            FarmEvent.ToggleTimer -> toggleTimer()
            FarmEvent.ResetTimer -> resetTimer()
            FarmEvent.CreateJournalEntry -> createJournal()
            is FarmEvent.SelectJournalEntry -> selectJournal(event.entryId)
            is FarmEvent.SelectWorkshopMode -> _state.update { it.copy(workshopMode = event.mode, message = null, journalDetailsExpanded = false, scrapbookToolsExpanded = false) }
            is FarmEvent.UpdateJournalTitle -> updateJournalTitle(event.text, event.committed)
            is FarmEvent.UpdateWorkshopText -> updateWorkshopText(event.text, event.committed)
            is FarmEvent.PressWorkshopKey -> pressWorkshopKey(event)
            is FarmEvent.SelectChineseCandidate -> selectChineseCandidate(event.text)
            FarmEvent.ToggleWorkshopInputMode -> toggleWorkshopInputMode()
            FarmEvent.WorkshopBackspace -> workshopBackspace()
            FarmEvent.CompleteJournalEntry -> completeJournal()
            FarmEvent.ToggleGestureKeyboard -> _state.update { it.copy(gestureKeyboardExpanded = !it.gestureKeyboardExpanded) }
            FarmEvent.ToggleJournalDetails -> _state.update { it.copy(journalDetailsExpanded = !it.journalDetailsExpanded, scrapbookToolsExpanded = false) }
            FarmEvent.ToggleScrapbookTools -> _state.update { it.copy(scrapbookToolsExpanded = !it.scrapbookToolsExpanded, journalDetailsExpanded = false) }
            is FarmEvent.SetJournalMood -> updateJournalMetadata { it.copy(mood = event.mood) }
            is FarmEvent.ToggleJournalTag -> toggleJournalTag(event.tag)
            is FarmEvent.ToggleJournalSticker -> toggleJournalSticker(event.stickerId)
            is FarmEvent.SetJournalBorder -> updateJournalMetadata { it.copy(borderStyle = event.borderStyle) }
            is FarmEvent.SetJournalPageTheme -> updateJournalMetadata { it.copy(pageTheme = event.pageTheme) }
            is FarmEvent.SetJournalLocalImage -> updateJournalMetadata("本地图片已自动保存") { it.copy(localImageUri = event.uri) }
            FarmEvent.ClearMessage -> _state.update { it.copy(message = null) }
            FarmEvent.ToggleTheme -> toggleTheme()
        }
    }

    private fun selectSpatialMode(mode: SpatialExperienceMode) {
        if (_state.value.game.activeJournalEntryId == null) {
            createJournal(openWorkshop = true)
        } else if (_state.value.selectedSection != FarmSection.WORKSHOP) {
            openWorkshop()
        }
        _state.update {
            it.copy(
                spatialMode = mode,
                selectedSection = FarmSection.WORKSHOP,
                workshopMode = if (mode == SpatialExperienceMode.REVIEW) WorkshopMode.ARCHIVE else WorkshopMode.WRITE,
                scrapbookToolsExpanded = mode == SpatialExperienceMode.SCRAPBOOK,
                journalDetailsExpanded = false,
                message = null,
            )
        }
    }

    private fun selectSection(section: FarmSection) {
        if (section == FarmSection.WORKSHOP) {
            openWorkshop()
        } else {
            _state.update { it.copy(selectedSection = section, message = null) }
        }
    }

    private fun openWorkshop() {
        val game = _state.value.game
        val activeId = game.activeJournalEntryId?.takeIf { id -> game.journalEntries.any { it.id == id } }
        if (activeId != null) {
            _state.update {
                it.copy(
                    selectedSection = FarmSection.WORKSHOP,
                    workshopMode = WorkshopMode.WRITE,
                    message = null,
                    gestureKeyboardExpanded = false,
                )
            }
            return
        }
        val latest = game.journalEntries.maxByOrNull { it.updatedAtMillis }
        if (latest != null) {
            val next = game.copy(activeJournalEntryId = latest.id)
            updateGame(next, null)
            _state.update { it.copy(selectedSection = FarmSection.WORKSHOP, workshopMode = WorkshopMode.WRITE) }
        } else {
            createJournal(openWorkshop = true)
        }
    }

    private fun createJournal(openWorkshop: Boolean = false) {
        val now = timeProvider()
        val dateKey = dateKeyProvider(now)
        val prompt = runCatching { dailyPrompt(LocalDate.parse(dateKey)) }.getOrDefault(dailyPrompt(currentDate()))
        val result = createJournalEntry(_state.value.game, now, dateKey, prompt)
        _state.update {
            it.withDerivedGame(result.state, result.message).copy(
                selectedSection = if (openWorkshop) FarmSection.WORKSHOP else it.selectedSection,
                workshopMode = WorkshopMode.WRITE,
                pinyinBuffer = "",
                gestureKeyboardExpanded = false,
                journalDetailsExpanded = false,
                scrapbookToolsExpanded = false,
            )
        }
        repository.save(result.state)
    }

    private fun selectJournal(entryId: String) {
        val game = _state.value.game
        if (game.journalEntries.none { it.id == entryId }) return
        val next = game.copy(activeJournalEntryId = entryId)
        _state.update { it.withDerivedGame(next, null).copy(workshopMode = WorkshopMode.WRITE, pinyinBuffer = "", journalDetailsExpanded = false, scrapbookToolsExpanded = false) }
        repository.save(next)
    }

    private fun handlePlotTap(event: FarmEvent.TapPlot) {
        val application = applyFarmKey(_state.value.game, event.plotId, event.keyLabel)
        _state.update {
            it.withDerivedGame(application.result.state, application.result.message).copy(
                wpm = application.wpm,
                lastKeyLabel = event.keyLabel,
            )
        }
        repository.save(application.result.state)
    }

    private fun handleSpeciesSelection(itemId: String) {
        val result = selectSpecies(_state.value.game, itemId)
        _state.update {
            it.withDerivedGame(result.state, result.message).copy(
                selectedSection = if (result.selected) FarmSection.FARM else it.selectedSection,
            )
        }
        repository.save(result.state)
    }

    private fun handlePurchase(item: com.example.fingertipfarm.domain.model.CatalogItem) {
        val result = purchaseItem(_state.value.game, item)
        updateGame(result.state, result.message)
    }

    private fun toggleTimer() {
        val game = _state.value.game
        if (game.timerRunning) {
            timerJob?.cancel()
            updateGame(game.copy(timerRunning = false), "专注计时已暂停")
        } else {
            val seconds = if (game.timerSeconds <= 0) FarmDefaults.FocusDurationSeconds else game.timerSeconds
            updateGame(game.copy(timerSeconds = seconds, timerRunning = true), "专注时间开始")
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob =
            viewModelScope.launch(timerDispatcher) {
                while (isActive && _state.value.game.timerRunning) {
                    delay(1_000)
                    val current = _state.value.game
                    if (!current.timerRunning) break
                    val remaining = (current.timerSeconds - 1).coerceAtLeast(0)
                    val finished = remaining == 0
                    updateGame(
                        current.copy(timerSeconds = remaining, timerRunning = !finished),
                        message = if (finished) "25 分钟专注完成" else _state.value.message,
                    )
                    if (finished) break
                }
            }
    }

    private fun startWpmTicker() {
        wpmJob?.cancel()
        wpmJob =
            viewModelScope.launch(timerDispatcher) {
                while (isActive) {
                    delay(1_000)
                    refreshWpm()
                }
            }
    }

    private fun startPartnerAutomation() {
        partnerJob?.cancel()
        partnerJob =
            viewModelScope.launch(timerDispatcher) {
                var catTick = 0
                while (isActive) {
                    delay(SheepPatrolIntervalMs)
                    val ui = _state.value
                    val current = ui.game
                    val sheepOwned = "sheep" in current.ownedPartnerIds
                    val nextSheepStep =
                        if (sheepOwned) (ui.sheepPatrolStep + 1).mod(FarmDefaults.SheepPatrolPlotIndices.size)
                        else ui.sheepPatrolStep
                    val patrolPlotIndex = FarmDefaults.SheepPatrolPlotIndices.getOrElse(nextSheepStep) { 0 }
                    val patrolPlotId = current.plots.getOrNull(patrolPlotIndex)?.id.orEmpty()
                    val sheep = partnerAutomation.runSheepPatrol(current, patrolPlotId, current.protectedReadyPlotIds())
                    var next = sheep.state
                    var message = sheep.message
                    if (sheep.harvestedPlotIds.isNotEmpty()) {
                        val memories = createMemoryCard(next, sheep.harvestedPlotIds, timeProvider())
                        next = memories.state
                        if (memories.createdCount > 0) message = "${sheep.message} · 记忆卡 +${memories.createdCount}"
                    }

                    catTick += 1
                    var nextCatPulse = ui.catFishingPulse
                    if (catTick >= CatFishingTickCount) {
                        catTick = 0
                        val cat = partnerAutomation.runCatFishing(next)
                        next = cat.state
                        if (cat.message != null) {
                            message = cat.message
                            nextCatPulse += 1
                        }
                    }

                    if (next != current) updateGame(next, message)
                    if (sheepOwned || nextCatPulse != ui.catFishingPulse) {
                        _state.update {
                            it.copy(
                                sheepPatrolStep = nextSheepStep,
                                catFishingPulse = nextCatPulse,
                            )
                        }
                    }
                }
            }
    }

    internal fun refreshWpm() {
        val wpm = calculateWpm(timeProvider())
        _state.update { if (it.wpm == wpm) it else it.copy(wpm = wpm) }
    }

    private fun resetTimer() {
        timerJob?.cancel()
        updateGame(
            _state.value.game.copy(timerSeconds = FarmDefaults.FocusDurationSeconds, timerRunning = false),
            "专注计时已重置",
        )
    }

    private fun toggleTheme() {
        val game = _state.value.game
        val next = if (game.theme == FarmTheme.DAY) FarmTheme.NIGHT else FarmTheme.DAY
        updateGame(game.copy(theme = next), if (next == FarmTheme.NIGHT) "夜幕降临" else "新的白天开始了")
    }

    private fun updateJournalTitle(text: String, committed: Boolean) {
        if (!committed) return
        val game = _state.value.game
        val entry = game.activeJournal() ?: return
        val now = timeProvider()
        val title = text.replace('\n', ' ').take(MaxJournalTitleLength)
        val updated = entry.copy(title = title, updatedAtMillis = now)
        val next = game.copy(journalEntries = game.journalEntries.map { if (it.id == entry.id) updated else it })
        updateGame(next, "已自动保存")
    }

    private fun updateWorkshopText(text: String, committed: Boolean) {
        if (!committed) return
        val current = _state.value.game
        val entry = current.activeJournal() ?: return
        val nextText = text.take(MaxWorkshopTextLength)
        val inserted = insertedText(entry.body, nextText)
        val keyCount = inserted.codePointCount(0, inserted.length)
        val inputRate = registerKeyPresses(keyCount)
        val typing =
            applyTypingGrowth(
                state = current,
                previousText = entry.body,
                nextText = nextText,
                protectedReadyPlotIds = current.protectedReadyPlotIds(),
            )
        val writing = applyJournalWritingReward(typing.state, entry.id, nextText, timeProvider())
        val memories = createMemoryCard(writing.state, typing.harvestedPlotIds, timeProvider())
        val message =
            when {
                memories.createdCount > 0 -> "收获了 ${memories.createdCount} 张记忆卡"
                writing.appliedGrowth > 0 -> "已自动保存 · 记忆作物成长 +${writing.appliedGrowth}"
                else -> typing.message?.let { "已自动保存 · $it" } ?: "已自动保存"
            }
        _state.update {
            it.withDerivedGame(memories.state, message).copy(
                wpm = inputRate.wpm,
                lastKeyLabel = typing.lastKeyLabel ?: it.lastKeyLabel,
                pinyinBuffer = "",
            )
        }
        repository.save(memories.state)
    }

    private fun pressWorkshopKey(event: FarmEvent.PressWorkshopKey) {
        val ui = _state.value
        val entry = ui.game.activeJournal() ?: return
        val key = event.keyLabel
        val application = applyFarmKey(ui.game, event.plotId, key)
        var nextGame = application.result.state
        var nextPinyin = ui.pinyinBuffer
        var message = application.result.message
        var nextBody: String? = null

        if (nextGame.workshopInputMode == WorkshopInputMode.CHINESE && key.length == 1 && key.first().isLetter()) {
            nextPinyin = (nextPinyin + key.lowercase()).take(MaxPinyinLength)
        } else if (nextGame.workshopInputMode == WorkshopInputMode.CHINESE && nextPinyin.isNotEmpty()) {
            val committedPinyin = resolvePinyin(nextPinyin)
            val suffix = if (key == "SPACE" || key == "ENTER") "" else keyToText(key, nextGame.workshopInputMode)
            nextBody = (entry.body + committedPinyin + suffix).take(MaxWorkshopTextLength)
            nextPinyin = ""
            message = "已输入：$committedPinyin"
        } else {
            nextBody = (entry.body + keyToText(key, nextGame.workshopInputMode)).take(MaxWorkshopTextLength)
        }

        if (nextBody != null) {
            val writing = applyJournalWritingReward(nextGame, entry.id, nextBody, timeProvider())
            nextGame = writing.state
            if (writing.appliedGrowth > 0) message = "已自动保存 · 记忆作物成长 +${writing.appliedGrowth}"
        }
        _state.update {
            it.withDerivedGame(nextGame, message).copy(
                wpm = application.wpm,
                lastKeyLabel = key,
                pinyinBuffer = nextPinyin,
            )
        }
        repository.save(nextGame)
    }

    private fun selectChineseCandidate(candidate: String) {
        val game = _state.value.game
        val entry = game.activeJournal() ?: return
        val nextText = (entry.body + candidate).take(MaxWorkshopTextLength)
        val writing = applyJournalWritingReward(game, entry.id, nextText, timeProvider())
        updateGame(writing.state, "已输入：$candidate · 自动保存")
        _state.update { it.copy(pinyinBuffer = "") }
    }

    private fun toggleWorkshopInputMode() {
        val ui = _state.value
        val entry = ui.game.activeJournal() ?: return
        var nextGame = ui.game
        if (ui.pinyinBuffer.isNotEmpty()) {
            val committedText = (entry.body + resolvePinyin(ui.pinyinBuffer)).take(MaxWorkshopTextLength)
            nextGame = applyJournalWritingReward(nextGame, entry.id, committedText, timeProvider()).state
        }
        val next = if (nextGame.workshopInputMode == WorkshopInputMode.ENGLISH) WorkshopInputMode.CHINESE else WorkshopInputMode.ENGLISH
        nextGame = nextGame.copy(workshopInputMode = next)
        updateGame(nextGame, if (next == WorkshopInputMode.CHINESE) "中文拼音输入" else "English input")
        _state.update { it.copy(pinyinBuffer = "") }
    }

    private fun workshopBackspace() {
        val ui = _state.value
        if (ui.pinyinBuffer.isNotEmpty()) {
            _state.update { it.copy(pinyinBuffer = it.pinyinBuffer.dropLast(1), message = null) }
            return
        }
        val entry = ui.game.activeJournal() ?: return
        if (entry.body.isEmpty()) return
        val end = entry.body.offsetByCodePoints(entry.body.length, -1)
        val writing = applyJournalWritingReward(ui.game, entry.id, entry.body.substring(0, end), timeProvider())
        updateGame(writing.state, "已撤回最后一个字符 · 自动保存")
    }

    private fun completeJournal() {
        val game = _state.value.game
        val entry = game.activeJournal() ?: return
        val result = completeJournalEntry(game, entry.id, timeProvider())
        updateGame(result.state, result.message)
    }

    private fun applyFarmKey(game: FarmGameState, plotId: String, keyLabel: String?): KeyApplication {
        val inputRate = registerKeyPresses(1)
        val plot = game.plots.firstOrNull { it.id == plotId }
        val protected = plot?.status == PlotStatus.READY && plotId in game.protectedReadyPlotIds()
        if (protected) {
            return KeyApplication(
                result = PlotInteractionResult(game, "先在工坊完成这篇日记，再收获它的记忆作物"),
                wpm = inputRate.wpm,
            )
        }
        val raw = interactWithPlot(game, plotId)
        val harvestedPlotIds =
            if (plot?.status == PlotStatus.READY && raw.state.plots.firstOrNull { it.id == plotId }?.status == PlotStatus.EMPTY) {
                listOf(plotId)
            } else {
                emptyList()
            }
        val memories = createMemoryCard(raw.state, harvestedPlotIds, timeProvider())
        val counted = if (keyLabel != null && keyLabel != "SPACE" && keyLabel != "ENTER") 1 else 0
        val state = memories.state.copy(typedCharacterTotal = memories.state.typedCharacterTotal + counted)
        val message = if (memories.createdCount > 0) "${raw.message} · 记忆卡 +${memories.createdCount}" else raw.message
        return KeyApplication(result = raw.copy(state = state, message = message), wpm = inputRate.wpm)
    }

    private fun registerKeyPresses(count: Int): InputRate {
        if (count <= 0) return InputRate(_state.value.wpm)
        val now = timeProvider()
        repeat(count) { inputPressTimes.addLast(now) }
        return InputRate(wpm = calculateWpm(now))
    }

    private fun calculateWpm(now: Long): Int {
        while (inputPressTimes.firstOrNull()?.let { now - it > WpmWindowMs } == true) inputPressTimes.removeFirst()
        return inputPressTimes.size * 6
    }

    private fun resolvePinyin(pinyin: String): String = WorkshopPinyin.candidates(pinyin).firstOrNull() ?: pinyin

    private fun keyToText(key: String, mode: WorkshopInputMode): String =
        when (key) {
            "SPACE" -> " "
            "ENTER" -> "\n"
            "," -> if (mode == WorkshopInputMode.CHINESE) "，" else ","
            "." -> if (mode == WorkshopInputMode.CHINESE) "。" else "."
            "/" -> if (mode == WorkshopInputMode.CHINESE) "、" else "/"
            ";" -> if (mode == WorkshopInputMode.CHINESE) "；" else ";"
            "'" -> if (mode == WorkshopInputMode.CHINESE) "’" else "'"
            else -> key.lowercase()
        }

    private fun insertedText(previous: String, next: String): String {
        val prefixLength = previous.zip(next).takeWhile { (old, new) -> old == new }.size
        val previousTail = previous.drop(prefixLength)
        val nextTail = next.drop(prefixLength)
        val suffixLength = previousTail.reversed().zip(nextTail.reversed()).takeWhile { (old, new) -> old == new }.size
        return next.substring(prefixLength, next.length - suffixLength)
    }

    private fun FarmGameState.activeJournal(): JournalEntry? =
        journalEntries.firstOrNull { it.id == activeJournalEntryId }

    private fun FarmGameState.protectedReadyPlotIds(): Set<String> =
        journalEntries
            .filter { entry -> entry.completedAtMillis == null && memoryCards.none { it.journalEntryId == entry.id } }
            .mapTo(mutableSetOf()) { it.linkedPlotId }

    private fun updateJournalMetadata(
        message: String = "已自动保存",
        transform: (JournalEntry) -> JournalEntry,
    ) {
        val game = _state.value.game
        val entry = game.activeJournal() ?: return
        val updated = transform(entry).copy(updatedAtMillis = timeProvider())
        val next = game.copy(journalEntries = game.journalEntries.map { if (it.id == entry.id) updated else it })
        updateGame(next, message)
    }

    private fun toggleJournalTag(tag: String) {
        updateJournalMetadata {
            val next = if (tag in it.tags) it.tags - tag else if (it.tags.size < MaxJournalTags) it.tags + tag else it.tags
            it.copy(tags = next)
        }
    }

    private fun toggleJournalSticker(stickerId: String) {
        updateJournalMetadata {
            val next = if (stickerId in it.stickerIds) it.stickerIds - stickerId else if (it.stickerIds.size < MaxJournalStickers) it.stickerIds + stickerId else it.stickerIds
            it.copy(stickerIds = next)
        }
    }

    private fun currentDate(): LocalDate =
        runCatching { LocalDate.parse(dateKeyProvider(timeProvider())) }
            .getOrElse { Instant.ofEpochMilli(timeProvider()).atZone(ZoneId.systemDefault()).toLocalDate() }

    private fun FarmUiState.withDerivedGame(game: FarmGameState, message: String?): FarmUiState {
        val today = currentDate()
        val prompt = dailyPrompt(today)
        val reflection = reflectionGarden(game, today)
        val activeMood = game.journalEntries.firstOrNull { it.id == game.activeJournalEntryId }?.mood
        val localHour = Instant.ofEpochMilli(timeProvider()).atZone(ZoneId.systemDefault()).hour
        return copy(
            game = game,
            message = message,
            todayDateKey = today.toString(),
            dailyPrompt = prompt,
            reflection = reflection,
            partnerAbilities = partnerAbility(game, reflection, prompt),
            spatialAmbience = resolveSpatialAmbience(localHour, activeMood),
        )
    }

    private fun updateGame(game: FarmGameState, message: String?) {
        _state.update { it.withDerivedGame(game, message) }
        repository.save(game)
    }

    override fun onCleared() {
        timerJob?.cancel()
        wpmJob?.cancel()
        partnerJob?.cancel()
        repository.save(_state.value.game.copy(timerRunning = false))
        super.onCleared()
    }

    private data class InputRate(val wpm: Int)
    private data class KeyApplication(val result: PlotInteractionResult, val wpm: Int)

    companion object {
        private const val WpmWindowMs = 10_000L
        private const val SheepPatrolIntervalMs = 3_000L
        private const val CatFishingTickCount = 10
        private const val MaxWorkshopTextLength = 2_000
        private const val MaxJournalTitleLength = 48
        private const val MaxPinyinLength = 24
        private const val MaxJournalTags = 3
        private const val MaxJournalStickers = 3

        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FarmViewModel(SharedPreferencesFarmRepository(context.applicationContext)) as T
            }
    }
}
