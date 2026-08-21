package com.example.fingertipfarm.data.repository

import android.content.Context
import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.FarmTheme
import com.example.fingertipfarm.domain.model.JournalEntry
import com.example.fingertipfarm.domain.model.JournalBorderStyle
import com.example.fingertipfarm.domain.model.JournalMood
import com.example.fingertipfarm.domain.model.JournalPageTheme
import com.example.fingertipfarm.domain.model.MemoryCard
import com.example.fingertipfarm.domain.model.PlotState
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType
import com.example.fingertipfarm.domain.model.WorkshopInputMode
import java.nio.charset.StandardCharsets
import java.util.Base64

class SharedPreferencesFarmRepository(context: Context) : FarmRepository {
    private val preferences =
        context.getSharedPreferences("fingertip_farm_spatial_save", Context.MODE_PRIVATE)

    override fun load(): FarmGameState {
        val defaults = FarmDefaults.initialState()
        val activeCropId = preferences.getString(KEY_ACTIVE_CROP, defaults.activeCropId) ?: defaults.activeCropId
        val activeFishId = preferences.getString(KEY_ACTIVE_FISH, defaults.activeFishId) ?: defaults.activeFishId
        val loadedPlots = migratePlots(decodePlots(preferences.getString(KEY_PLOTS, null)), defaults.plots)
        val migration =
            migrateLegacyJournal(
                entries = decodeJournalEntries(preferences.getString(KEY_JOURNAL_ENTRIES, null)),
                legacyText = preferences.getString(KEY_WORKSHOP_TEXT, "").orEmpty(),
                plots = loadedPlots,
                activeCropId = activeCropId,
            )
        val activeJournalId =
            preferences.getString(KEY_ACTIVE_JOURNAL_ENTRY_ID, null)
                ?.takeIf { id -> migration.entries.any { it.id == id } }
                ?: migration.entries.maxByOrNull { it.updatedAtMillis }?.id

        return defaults.copy(
            gold = preferences.getInt(KEY_GOLD, defaults.gold).coerceAtLeast(0),
            activeCropId = activeCropId,
            activeFishId = activeFishId,
            unlockedCropIds = preferences.getStringSet(KEY_UNLOCKED_CROPS, defaults.unlockedCropIds)?.toSet()
                ?: defaults.unlockedCropIds,
            unlockedFishIds = preferences.getStringSet(KEY_UNLOCKED_FISH, defaults.unlockedFishIds)?.toSet()
                ?: defaults.unlockedFishIds,
            ownedPartnerIds = preferences.getStringSet(KEY_OWNED_PARTNERS, emptySet())?.toSet().orEmpty(),
            plots = migration.plots,
            journalEntries = migration.entries,
            activeJournalEntryId = activeJournalId,
            memoryCards = decodeMemoryCards(preferences.getString(KEY_MEMORY_CARDS, null)),
            typedCharacterTotal = preferences.getInt(KEY_TYPED_CHARACTER_TOTAL, 0).coerceAtLeast(0),
            timerSeconds = preferences.getInt(KEY_TIMER_SECONDS, FarmDefaults.FocusDurationSeconds)
                .coerceIn(0, FarmDefaults.FocusDurationSeconds),
            timerRunning = false,
            theme = runCatching {
                FarmTheme.valueOf(preferences.getString(KEY_THEME, defaults.theme.name) ?: defaults.theme.name)
            }.getOrDefault(defaults.theme),
            workshopInputMode = runCatching {
                WorkshopInputMode.valueOf(
                    preferences.getString(KEY_WORKSHOP_INPUT_MODE, defaults.workshopInputMode.name)
                        ?: defaults.workshopInputMode.name,
                )
            }.getOrDefault(defaults.workshopInputMode),
        )
    }

    override fun save(state: FarmGameState) {
        preferences.edit()
            .putInt(KEY_GOLD, state.gold)
            .putString(KEY_ACTIVE_CROP, state.activeCropId)
            .putString(KEY_ACTIVE_FISH, state.activeFishId)
            .putStringSet(KEY_UNLOCKED_CROPS, state.unlockedCropIds.toMutableSet())
            .putStringSet(KEY_UNLOCKED_FISH, state.unlockedFishIds.toMutableSet())
            .putStringSet(KEY_OWNED_PARTNERS, state.ownedPartnerIds.toMutableSet())
            .putString(KEY_PLOTS, encodePlots(state.plots))
            .putString(KEY_JOURNAL_ENTRIES, encodeJournalEntries(state.journalEntries))
            .putString(KEY_ACTIVE_JOURNAL_ENTRY_ID, state.activeJournalEntryId)
            .putString(KEY_MEMORY_CARDS, encodeMemoryCards(state.memoryCards))
            .putString(
                KEY_WORKSHOP_TEXT,
                state.journalEntries.firstOrNull { it.id == state.activeJournalEntryId }?.body.orEmpty(),
            )
            .putInt(KEY_TYPED_CHARACTER_TOTAL, state.typedCharacterTotal)
            .putInt(KEY_TIMER_SECONDS, state.timerSeconds)
            .putString(KEY_THEME, state.theme.name)
            .putString(KEY_WORKSHOP_INPUT_MODE, state.workshopInputMode.name)
            .apply()
    }

    private fun encodeJournalEntries(entries: List<JournalEntry>): String =
        entries.joinToString("|") { entry ->
            listOf(
                encodeText(entry.id),
                encodeText(entry.dateKey),
                encodeText(entry.title),
                encodeText(entry.body),
                encodeText(entry.cropId),
                encodeText(entry.linkedPlotId),
                entry.createdAtMillis.toString(),
                entry.updatedAtMillis.toString(),
                entry.rewardedMilestones.toString(),
                entry.completedAtMillis?.toString().orEmpty(),
                entry.mood.name,
                encodeText(entry.tags.sorted().joinToString(SET_SEPARATOR)),
                encodeText(entry.dailyPrompt),
                encodeText(entry.stickerIds.sorted().joinToString(SET_SEPARATOR)),
                entry.borderStyle.name,
                entry.pageTheme.name,
                encodeText(entry.localImageUri.orEmpty()),
            ).joinToString(",")
        }

    private fun decodeJournalEntries(raw: String?): List<JournalEntry> =
        raw?.takeIf { it.isNotBlank() }?.split('|')?.mapNotNull { encoded ->
            val fields = encoded.split(',', limit = 17)
            if (fields.size != 10 && fields.size != 17) return@mapNotNull null
            runCatching {
                JournalEntry(
                    id = decodeText(fields[0]),
                    dateKey = decodeText(fields[1]),
                    title = decodeText(fields[2]),
                    body = decodeText(fields[3]),
                    cropId = decodeText(fields[4]),
                    linkedPlotId = decodeText(fields[5]),
                    createdAtMillis = fields[6].toLong(),
                    updatedAtMillis = fields[7].toLong(),
                    rewardedMilestones = fields[8].toInt().coerceAtLeast(0),
                    completedAtMillis = fields[9].takeIf { it.isNotBlank() }?.toLong(),
                    mood = fields.getOrNull(10)?.let(JournalMood::valueOf) ?: JournalMood.CALM,
                    tags = fields.getOrNull(11)?.let(::decodeText)?.split(SET_SEPARATOR)?.filter(String::isNotBlank)?.toSet().orEmpty(),
                    dailyPrompt = fields.getOrNull(12)?.let(::decodeText).orEmpty(),
                    stickerIds = fields.getOrNull(13)?.let(::decodeText)?.split(SET_SEPARATOR)?.filter(String::isNotBlank)?.toSet().orEmpty(),
                    borderStyle = fields.getOrNull(14)?.let(JournalBorderStyle::valueOf) ?: JournalBorderStyle.CLASSIC,
                    pageTheme = fields.getOrNull(15)?.let(JournalPageTheme::valueOf) ?: JournalPageTheme.CREAM,
                    localImageUri = fields.getOrNull(16)?.let(::decodeText)?.ifBlank { null },
                )
            }.getOrNull()
        }.orEmpty()

    private fun encodeMemoryCards(cards: List<MemoryCard>): String =
        cards.joinToString("|") { card ->
            listOf(encodeText(card.id), encodeText(card.journalEntryId), card.harvestedAtMillis.toString()).joinToString(",")
        }

    private fun decodeMemoryCards(raw: String?): List<MemoryCard> =
        raw?.takeIf { it.isNotBlank() }?.split('|')?.mapNotNull { encoded ->
            val fields = encoded.split(',', limit = 3)
            if (fields.size != 3) return@mapNotNull null
            runCatching {
                MemoryCard(
                    id = decodeText(fields[0]),
                    journalEntryId = decodeText(fields[1]),
                    harvestedAtMillis = fields[2].toLong(),
                )
            }.getOrNull()
        }.orEmpty()

    private fun encodeText(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeText(value: String): String =
        String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)

    private fun migrateLegacyJournal(
        entries: List<JournalEntry>,
        legacyText: String,
        plots: List<PlotState>,
        activeCropId: String,
    ): LegacyJournalMigration {
        if (entries.isNotEmpty() || legacyText.isBlank()) return LegacyJournalMigration(entries, plots)
        val plot = plots.firstOrNull { it.type == PlotType.SOIL } ?: return LegacyJournalMigration(entries, plots)
        val now = System.currentTimeMillis()
        val cropId = plot.contentId ?: activeCropId
        val migratedPlot =
            if (plot.status == PlotStatus.EMPTY) {
                plot.copy(contentId = cropId, growth = 0, status = PlotStatus.GROWING)
            } else {
                plot
            }
        val entry =
            JournalEntry(
                id = "journal_legacy_$now",
                dateKey = "旧日记",
                title = "迁移的旧日记",
                body = legacyText,
                cropId = cropId,
                linkedPlotId = plot.id,
                createdAtMillis = now,
                updatedAtMillis = now,
            )
        return LegacyJournalMigration(
            entries = listOf(entry),
            plots = plots.map { if (it.id == plot.id) migratedPlot else it },
        )
    }

    private fun encodePlots(plots: List<PlotState>): String =
        plots.joinToString("|") { plot ->
            listOf(
                plot.id,
                plot.type.name,
                plot.contentId.orEmpty(),
                plot.growth.toString(),
                plot.count.toString(),
                plot.status.name,
            ).joinToString(",")
        }

    private fun decodePlots(raw: String?): List<PlotState>? =
        raw?.split('|')?.mapNotNull { encoded ->
            val fields = encoded.split(',')
            if (fields.size != 6) return@mapNotNull null
            runCatching {
                PlotState(
                    id = fields[0],
                    type = PlotType.valueOf(fields[1]),
                    contentId = fields[2].ifBlank { null },
                    growth = fields[3].toInt().coerceAtLeast(0),
                    count = fields[4].toInt().coerceAtLeast(0),
                    status = PlotStatus.valueOf(fields[5]),
                )
            }.getOrNull()
        }

    private fun migratePlots(
        saved: List<PlotState>?,
        defaults: List<PlotState>,
    ): List<PlotState> {
        if (saved == null) return defaults
        if (saved.size == defaults.size) return saved

        val savedSoil = saved.filter { it.type == PlotType.SOIL }.iterator()
        val savedPonds = saved.filter { it.type == PlotType.POND }.iterator()
        return defaults.map { default ->
            val old =
                when (default.type) {
                    PlotType.SOIL -> if (savedSoil.hasNext()) savedSoil.next() else null
                    PlotType.POND -> if (savedPonds.hasNext()) savedPonds.next() else null
                }
            old?.copy(id = default.id, type = default.type) ?: default
        }
    }

    private companion object {
        const val SET_SEPARATOR = "\u001F"
        const val KEY_GOLD = "gold"
        const val KEY_ACTIVE_CROP = "active_crop"
        const val KEY_ACTIVE_FISH = "active_fish"
        const val KEY_UNLOCKED_CROPS = "unlocked_crops"
        const val KEY_UNLOCKED_FISH = "unlocked_fish"
        const val KEY_OWNED_PARTNERS = "owned_partners"
        const val KEY_PLOTS = "plots"
        const val KEY_WORKSHOP_TEXT = "workshop_text"
        const val KEY_JOURNAL_ENTRIES = "journal_entries_v1"
        const val KEY_ACTIVE_JOURNAL_ENTRY_ID = "active_journal_entry_id"
        const val KEY_MEMORY_CARDS = "memory_cards_v1"
        const val KEY_TYPED_CHARACTER_TOTAL = "typed_character_total"
        const val KEY_TIMER_SECONDS = "timer_seconds"
        const val KEY_THEME = "theme"
        const val KEY_WORKSHOP_INPUT_MODE = "workshop_input_mode"
    }

    private data class LegacyJournalMigration(
        val entries: List<JournalEntry>,
        val plots: List<PlotState>,
    )
}
