package com.example.fingertipfarm.ui.farm

import com.example.fingertipfarm.TestFarmRepository
import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.FarmTheme
import com.example.fingertipfarm.domain.model.JournalBorderStyle
import com.example.fingertipfarm.domain.model.JournalMood
import com.example.fingertipfarm.domain.model.JournalPageTheme
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.SpatialExperienceMode
import com.example.fingertipfarm.domain.model.WorkshopInputMode
import com.example.fingertipfarm.domain.usecase.InteractWithPlotUseCase
import com.example.fingertipfarm.ui.navigation.FarmSection
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FarmViewModelTest {
    @Test
    fun initLoadsRepositoryState() {
        val initial = FarmDefaults.initialState().copy(gold = 321)
        val viewModel = FarmViewModel(TestFarmRepository(initial), timerDispatcher = StandardTestDispatcher())

        assertEquals(321, viewModel.state.value.game.gold)
    }

    @Test
    fun spatialModesRouteToWritingScrapbookAndReviewStates() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))

        viewModel.onEvent(FarmEvent.SelectSpatialMode(SpatialExperienceMode.SCRAPBOOK))
        assertEquals(FarmSection.WORKSHOP, viewModel.state.value.selectedSection)
        assertEquals(WorkshopMode.WRITE, viewModel.state.value.workshopMode)
        assertTrue(viewModel.state.value.scrapbookToolsExpanded)

        viewModel.onEvent(FarmEvent.SelectSpatialMode(SpatialExperienceMode.REVIEW))
        assertEquals(WorkshopMode.ARCHIVE, viewModel.state.value.workshopMode)
        assertFalse(viewModel.state.value.scrapbookToolsExpanded)
        assertEquals(SpatialExperienceMode.REVIEW, viewModel.state.value.spatialMode)
    }

    @Test
    fun openingWorkshopCreatesDatedJournalAndPlantsLinkedCrop() {
        val repository = TestFarmRepository(FarmDefaults.initialState())
        val viewModel = testViewModel(repository)

        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))

        val entry = viewModel.activeEntry()
        assertEquals("2026-08-04", entry.dateKey)
        assertEquals("plot_1", entry.linkedPlotId)
        assertEquals(PlotStatus.GROWING, viewModel.state.value.game.plots.first().status)
        assertEquals(FarmSection.WORKSHOP, viewModel.state.value.selectedSection)
    }

    @Test
    fun creatingSecondJournalUsesAnotherEmptyPlot() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))
        val first = viewModel.activeEntry()

        viewModel.onEvent(FarmEvent.CreateJournalEntry)

        val second = viewModel.activeEntry()
        assertEquals(2, viewModel.state.value.game.journalEntries.size)
        assertNotEquals(first.linkedPlotId, second.linkedPlotId)
    }

    @Test
    fun committedSystemInputAutosavesJournalAndUpdatesMappedFarmPlot() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))

        viewModel.onEvent(FarmEvent.UpdateWorkshopText("q", committed = true))

        assertEquals("q", viewModel.activeEntry().body)
        assertEquals(PlotStatus.GROWING, viewModel.state.value.game.plots.first { it.id == "plot_13" }.status)
        assertTrue(viewModel.state.value.message.orEmpty().contains("自动保存"))
    }

    @Test
    fun uncommittedImeCompositionDoesNotDoubleCountGrowth() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))

        viewModel.onEvent(FarmEvent.UpdateWorkshopText("qin", committed = false))
        assertEquals("", viewModel.activeEntry().body)
        assertEquals(0, viewModel.state.value.game.typedCharacterTotal)

        viewModel.onEvent(FarmEvent.UpdateWorkshopText("秦", committed = true))
        assertEquals("秦", viewModel.activeEntry().body)
        assertEquals(1, viewModel.state.value.game.typedCharacterTotal)
    }

    @Test
    fun titleAndArchivedEntryCanBeReopenedForEditing() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))
        val firstId = viewModel.activeEntry().id
        viewModel.onEvent(FarmEvent.UpdateJournalTitle("今天的收获", committed = true))
        viewModel.onEvent(FarmEvent.CreateJournalEntry)

        viewModel.onEvent(FarmEvent.SelectJournalEntry(firstId))
        viewModel.onEvent(FarmEvent.UpdateWorkshopText("重新补写", committed = true))

        assertEquals(firstId, viewModel.state.value.game.activeJournalEntryId)
        assertEquals("今天的收获", viewModel.activeEntry().title)
        assertEquals("重新补写", viewModel.activeEntry().body)
        assertEquals(WorkshopMode.WRITE, viewModel.state.value.workshopMode)
    }

    @Test
    fun completingAndHarvestingLinkedCropCreatesReopenableMemoryCard() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))
        val entryId = viewModel.activeEntry().id
        repeat(12) { index ->
            viewModel.onEvent(FarmEvent.UpdateWorkshopText("a".repeat((index + 1) * 20), committed = true))
        }
        viewModel.onEvent(FarmEvent.CompleteJournalEntry)
        assertEquals(PlotStatus.READY, viewModel.state.value.game.plots.first { it.id == "plot_1" }.status)

        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.FARM))
        viewModel.onEvent(FarmEvent.TapPlot("plot_1", "1"))

        assertEquals(1, viewModel.state.value.game.memoryCards.size)
        assertEquals(entryId, viewModel.state.value.game.memoryCards.single().journalEntryId)
    }

    @Test
    fun blankJournalCannotBeCompleted() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))

        viewModel.onEvent(FarmEvent.CompleteJournalEntry)

        assertEquals(null, viewModel.activeEntry().completedAtMillis)
        assertTrue(viewModel.state.value.message.orEmpty().contains("先写下"))
    }

    @Test
    fun purchasingItemUnlocksItAndUpdatesMessage() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState().copy(gold = 200)))
        val tomato = FarmCatalog.crops.first { it.id == "tomato" }

        viewModel.onEvent(FarmEvent.Purchase(tomato))

        assertTrue("tomato" in viewModel.state.value.game.unlockedCropIds)
        assertTrue(viewModel.state.value.message.orEmpty().contains("番茄"))
    }

    @Test
    fun lockedSpeciesSelectionIsBoundarySafe() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))

        viewModel.onEvent(FarmEvent.SelectSpecies("shark"))

        assertEquals("goldfish", viewModel.state.value.game.activeFishId)
        assertTrue(viewModel.state.value.message.orEmpty().contains("尚未解锁"))
    }

    @Test
    fun workshopKeyFollowsPlotStateMachineAndWritesJournal() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))

        viewModel.onEvent(FarmEvent.PressWorkshopKey("plot_13", "Q"))
        viewModel.onEvent(FarmEvent.PressWorkshopKey("plot_13", "Q"))

        assertEquals("qq", viewModel.activeEntry().body)
        assertEquals(1, viewModel.state.value.game.plots.first { it.id == "plot_13" }.growth)
        assertEquals("Q", viewModel.state.value.lastKeyLabel)
    }

    @Test
    fun rapidTypingReportsWpmButNeverDoublesGrowth() {
        val repository = TestFarmRepository(FarmDefaults.initialState())
        var now = 1_000L
        val viewModel =
            FarmViewModel(
                repository = repository,
                interactWithPlot = InteractWithPlotUseCase(cropCareIncrement = 1),
                timerDispatcher = StandardTestDispatcher(),
                timeProvider = { now },
                dateKeyProvider = { "2026-08-04" },
            )
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))

        repeat(11) { viewModel.onEvent(FarmEvent.PressWorkshopKey("plot_13", "Q")) }

        assertEquals(66, viewModel.state.value.wpm)
        assertEquals(10, viewModel.state.value.game.plots.first { it.id == "plot_13" }.growth)
        now += 11_000L
        viewModel.refreshWpm()
        assertEquals(0, viewModel.state.value.wpm)
    }

    @Test
    fun chinesePinyinCandidateCommitsWithoutCountingCandidateAgain() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))
        viewModel.onEvent(FarmEvent.ToggleWorkshopInputMode)

        "JINTIAN".forEach { label ->
            val index = FarmDefaults.KeyboardLayout.flatten().indexOf(label.toString())
            viewModel.onEvent(FarmEvent.PressWorkshopKey("plot_${index + 1}", label.toString()))
        }
        assertEquals("jintian", viewModel.state.value.pinyinBuffer)
        assertEquals(7, viewModel.state.value.game.typedCharacterTotal)

        viewModel.onEvent(FarmEvent.PressWorkshopKey("plot_47", "SPACE"))

        assertEquals("今天", viewModel.activeEntry().body)
        assertEquals("", viewModel.state.value.pinyinBuffer)
        assertEquals(7, viewModel.state.value.game.typedCharacterTotal)
        assertEquals(WorkshopInputMode.CHINESE, viewModel.state.value.game.workshopInputMode)
    }

    @Test
    fun backspacePrefersPinyinCompositionBeforeJournalBody() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))
        viewModel.onEvent(FarmEvent.UpdateWorkshopText("农场日志", committed = true))
        viewModel.onEvent(FarmEvent.ToggleWorkshopInputMode)
        viewModel.onEvent(FarmEvent.PressWorkshopKey("plot_39", "N"))

        viewModel.onEvent(FarmEvent.WorkshopBackspace)

        assertEquals("农场日志", viewModel.activeEntry().body)
        assertEquals("", viewModel.state.value.pinyinBuffer)
        viewModel.onEvent(FarmEvent.WorkshopBackspace)
        assertEquals("农场日", viewModel.activeEntry().body)
    }

    @Test
    fun dayNightToggleUpdatesAndPersistsTheme() {
        val repository = TestFarmRepository(FarmDefaults.initialState())
        val viewModel = testViewModel(repository)

        viewModel.onEvent(FarmEvent.ToggleTheme)

        assertEquals(FarmTheme.NIGHT, viewModel.state.value.game.theme)
        assertEquals(FarmTheme.NIGHT, repository.current().theme)
    }

    @Test
    fun gestureKeyboardStartsCollapsedAndCanExpand() {
        val viewModel = testViewModel(TestFarmRepository(FarmDefaults.initialState()))
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))
        assertFalse(viewModel.state.value.gestureKeyboardExpanded)

        viewModel.onEvent(FarmEvent.ToggleGestureKeyboard)

        assertTrue(viewModel.state.value.gestureKeyboardExpanded)
    }

    @Test
    fun reflectionAndScrapbookMetadataAutosaveWithJournal() {
        val repository = TestFarmRepository(FarmDefaults.initialState())
        val viewModel = testViewModel(repository)
        viewModel.onEvent(FarmEvent.SelectSection(FarmSection.WORKSHOP))

        viewModel.onEvent(FarmEvent.SetJournalMood(JournalMood.GRATEFUL))
        viewModel.onEvent(FarmEvent.ToggleJournalTag("感恩"))
        viewModel.onEvent(FarmEvent.ToggleJournalSticker("leaf"))
        viewModel.onEvent(FarmEvent.SetJournalBorder(JournalBorderStyle.LEAF))
        viewModel.onEvent(FarmEvent.SetJournalPageTheme(JournalPageTheme.FOREST))
        viewModel.onEvent(FarmEvent.SetJournalLocalImage("content://journal/image"))

        val entry = viewModel.activeEntry()
        assertEquals(JournalMood.GRATEFUL, entry.mood)
        assertEquals(setOf("感恩"), entry.tags)
        assertEquals(setOf("leaf"), entry.stickerIds)
        assertEquals(JournalBorderStyle.LEAF, entry.borderStyle)
        assertEquals(JournalPageTheme.FOREST, entry.pageTheme)
        assertEquals("content://journal/image", repository.current().journalEntries.single().localImageUri)
        assertTrue(entry.dailyPrompt.isNotBlank())
        assertEquals(1, viewModel.state.value.reflection.weeklyEntryCount)
    }

    private fun testViewModel(repository: TestFarmRepository): FarmViewModel =
        FarmViewModel(
            repository = repository,
            timerDispatcher = StandardTestDispatcher(),
            timeProvider = { 1_722_700_800_000L },
            dateKeyProvider = { "2026-08-04" },
        )

    private fun FarmViewModel.activeEntry() =
        state.value.game.journalEntries.first { it.id == state.value.game.activeJournalEntryId }
}
