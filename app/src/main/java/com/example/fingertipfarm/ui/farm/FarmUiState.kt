package com.example.fingertipfarm.ui.farm

import com.example.fingertipfarm.domain.model.CatalogItem
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.FarmSeason
import com.example.fingertipfarm.domain.model.JournalBorderStyle
import com.example.fingertipfarm.domain.model.JournalMood
import com.example.fingertipfarm.domain.model.JournalPageTheme
import com.example.fingertipfarm.domain.model.PartnerAbilityNote
import com.example.fingertipfarm.domain.model.ReflectionGardenSummary
import com.example.fingertipfarm.domain.model.SpatialAmbience
import com.example.fingertipfarm.domain.model.SpatialAmbienceState
import com.example.fingertipfarm.domain.model.SpatialExperienceMode
import com.example.fingertipfarm.ui.navigation.FarmSection

data class FarmUiState(
    val game: FarmGameState,
    val selectedSection: FarmSection = FarmSection.FARM,
    val message: String? = null,
    val wpm: Int = 0,
    val lastKeyLabel: String? = null,
    val pinyinBuffer: String = "",
    val workshopMode: WorkshopMode = WorkshopMode.WRITE,
    val gestureKeyboardExpanded: Boolean = false,
    val journalDetailsExpanded: Boolean = false,
    val scrapbookToolsExpanded: Boolean = false,
    val todayDateKey: String = "",
    val dailyPrompt: String = "",
    val reflection: ReflectionGardenSummary = EmptyReflection,
    val partnerAbilities: List<PartnerAbilityNote> = emptyList(),
    val sheepPatrolStep: Int = -1,
    val catFishingPulse: Int = 0,
    val spatialMode: SpatialExperienceMode = SpatialExperienceMode.WRITING,
    val spatialAmbience: SpatialAmbienceState = SpatialAmbienceState(SpatialAmbience.DAY, "随真实时间"),
)

private val EmptyReflection = ReflectionGardenSummary(FarmSeason.SPRING, 0, 0, 0, 0, 0, 0, "")

enum class WorkshopMode {
    WRITE,
    ARCHIVE,
    MEMORIES,
}

sealed interface FarmEvent {
    data class SelectSpatialMode(val mode: SpatialExperienceMode) : FarmEvent

    data class SelectSection(val section: FarmSection) : FarmEvent

    data class TapPlot(
        val plotId: String,
        val keyLabel: String? = null,
    ) : FarmEvent

    data class SelectSpecies(val itemId: String) : FarmEvent

    data class Purchase(val item: CatalogItem) : FarmEvent

    data object ToggleTimer : FarmEvent

    data object ResetTimer : FarmEvent

    data object CreateJournalEntry : FarmEvent

    data class SelectJournalEntry(val entryId: String) : FarmEvent

    data class SelectWorkshopMode(val mode: WorkshopMode) : FarmEvent

    data class UpdateJournalTitle(val text: String, val committed: Boolean) : FarmEvent

    data class UpdateWorkshopText(val text: String, val committed: Boolean) : FarmEvent

    data class PressWorkshopKey(val plotId: String, val keyLabel: String) : FarmEvent

    data class SelectChineseCandidate(val text: String) : FarmEvent

    data object ToggleWorkshopInputMode : FarmEvent

    data object WorkshopBackspace : FarmEvent

    data object CompleteJournalEntry : FarmEvent

    data object ToggleGestureKeyboard : FarmEvent

    data object ToggleJournalDetails : FarmEvent

    data object ToggleScrapbookTools : FarmEvent

    data class SetJournalMood(val mood: JournalMood) : FarmEvent

    data class ToggleJournalTag(val tag: String) : FarmEvent

    data class ToggleJournalSticker(val stickerId: String) : FarmEvent

    data class SetJournalBorder(val borderStyle: JournalBorderStyle) : FarmEvent

    data class SetJournalPageTheme(val pageTheme: JournalPageTheme) : FarmEvent

    data class SetJournalLocalImage(val uri: String?) : FarmEvent

    data object ClearMessage : FarmEvent

    data object ToggleTheme : FarmEvent
}
