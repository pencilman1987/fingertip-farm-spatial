package com.example.fingertipfarm.ui.farm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fingertipfarm.ui.farm.components.FarmGrid
import com.example.fingertipfarm.ui.farm.components.FarmRoomOverlay
import com.example.fingertipfarm.ui.farm.components.InventoryContent
import com.example.fingertipfarm.ui.farm.components.MarketContent
import com.example.fingertipfarm.ui.farm.components.WorkshopContent
import com.example.fingertipfarm.ui.navigation.FarmSection

@Composable
fun FarmScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val farmViewModel: FarmViewModel = viewModel(factory = FarmViewModel.factory(context))
    val state by farmViewModel.state.collectAsStateWithLifecycle()
    FarmContent(
        state = state,
        onEvent = farmViewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
internal fun FarmContent(
    state: FarmUiState,
    onEvent: (FarmEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.selectedSection == FarmSection.WORKSHOP) {
        WorkshopContent(
            game = state.game,
            message = state.message,
            workshopMode = state.workshopMode,
            gestureKeyboardExpanded = state.gestureKeyboardExpanded,
            journalDetailsExpanded = state.journalDetailsExpanded,
            scrapbookToolsExpanded = state.scrapbookToolsExpanded,
            pinyinBuffer = state.pinyinBuffer,
            todayDateKey = state.todayDateKey,
            reflection = state.reflection,
            partnerAbilities = state.partnerAbilities,
            onToggleTimer = { onEvent(FarmEvent.ToggleTimer) },
            onCreateJournal = { onEvent(FarmEvent.CreateJournalEntry) },
            onSelectJournal = { onEvent(FarmEvent.SelectJournalEntry(it)) },
            onSelectMode = { onEvent(FarmEvent.SelectWorkshopMode(it)) },
            onTitleChange = { text, committed -> onEvent(FarmEvent.UpdateJournalTitle(text, committed)) },
            onTextChange = { text, committed -> onEvent(FarmEvent.UpdateWorkshopText(text, committed)) },
            onKeyPress = { plotId, keyLabel -> onEvent(FarmEvent.PressWorkshopKey(plotId, keyLabel)) },
            onSelectCandidate = { onEvent(FarmEvent.SelectChineseCandidate(it)) },
            onToggleInputMode = { onEvent(FarmEvent.ToggleWorkshopInputMode) },
            onBackspace = { onEvent(FarmEvent.WorkshopBackspace) },
            onCompleteJournal = { onEvent(FarmEvent.CompleteJournalEntry) },
            onToggleGestureKeyboard = { onEvent(FarmEvent.ToggleGestureKeyboard) },
            onToggleJournalDetails = { onEvent(FarmEvent.ToggleJournalDetails) },
            onToggleScrapbookTools = { onEvent(FarmEvent.ToggleScrapbookTools) },
            onSetMood = { onEvent(FarmEvent.SetJournalMood(it)) },
            onToggleTag = { onEvent(FarmEvent.ToggleJournalTag(it)) },
            onToggleSticker = { onEvent(FarmEvent.ToggleJournalSticker(it)) },
            onSetBorder = { onEvent(FarmEvent.SetJournalBorder(it)) },
            onSetPageTheme = { onEvent(FarmEvent.SetJournalPageTheme(it)) },
            onSetLocalImage = { onEvent(FarmEvent.SetJournalLocalImage(it)) },
            onBackToFarm = { onEvent(FarmEvent.SelectSection(FarmSection.FARM)) },
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 22.dp).then(modifier),
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 22.dp).then(modifier)) {
        FarmGrid(
            game = state.game,
            message = state.message,
            wpm = state.wpm,
            lastKeyLabel = state.lastKeyLabel,
            reflection = state.reflection,
            partnerAbilities = state.partnerAbilities,
            sheepPatrolStep = state.sheepPatrolStep,
            catFishingPulse = state.catFishingPulse,
            sceneInteractive = state.selectedSection == FarmSection.FARM,
            onClearMessage = { onEvent(FarmEvent.ClearMessage) },
            onToggleTheme = { onEvent(FarmEvent.ToggleTheme) },
            onPlotClick = { plotId, keyLabel -> onEvent(FarmEvent.TapPlot(plotId, keyLabel)) },
            onOpenSection = { onEvent(FarmEvent.SelectSection(it)) },
            modifier = Modifier.fillMaxSize(),
        )
        when (state.selectedSection) {
            FarmSection.FARM -> Unit
            FarmSection.INVENTORY ->
                FarmRoomOverlay(
                    title = "物种仓库",
                    subtitle = "选择下一次播种和投苗的品种",
                    gold = state.game.gold,
                    illustrationRes = com.example.fingertipfarm.R.drawable.farm_warehouse,
                    message = state.message,
                    onClose = { onEvent(FarmEvent.SelectSection(FarmSection.FARM)) },
                ) {
                    InventoryContent(
                        game = state.game,
                        onSelectSpecies = { onEvent(FarmEvent.SelectSpecies(it)) },
                    )
                }
            FarmSection.MARKET ->
                FarmRoomOverlay(
                    title = "农场集市",
                    subtitle = "用收获的金币解锁新品种和合伙人",
                    gold = state.game.gold,
                    illustrationRes = com.example.fingertipfarm.R.drawable.farm_market,
                    message = state.message,
                    onClose = { onEvent(FarmEvent.SelectSection(FarmSection.FARM)) },
                ) {
                    MarketContent(
                        game = state.game,
                        onPurchase = { onEvent(FarmEvent.Purchase(it)) },
                    )
                }
            FarmSection.WORKSHOP -> Unit
        }
    }
}
