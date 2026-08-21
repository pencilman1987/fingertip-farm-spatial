package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.PlotState
import com.example.fingertipfarm.domain.model.PlotStatus
import com.example.fingertipfarm.domain.model.PlotType

data class TypingGrowthResult(
    val state: FarmGameState,
    val insertedCharacters: Int,
    val processedKeyCount: Int,
    val appliedGrowth: Int,
    val plantedCount: Int,
    val harvestedCount: Int,
    val newlyReadyCount: Int,
    val harvestedPlotIds: List<String>,
    val lastKeyLabel: String?,
    val message: String?,
)

class ApplyTypingGrowthUseCase(
    private val interactWithPlot: InteractWithPlotUseCase = InteractWithPlotUseCase(),
) {
    private val keyLabels = FarmDefaults.KeyboardLayout.flatten()

    operator fun invoke(
        state: FarmGameState,
        previousText: String,
        nextText: String,
        growthMultiplier: Int = 1,
        protectedReadyPlotIds: Set<String> = emptySet(),
    ): TypingGrowthResult {
        val insertedTokens = insertedText(previousText, nextText).toCodePointStrings()
        if (insertedTokens.isEmpty()) {
            return TypingGrowthResult(
                state = state,
                insertedCharacters = 0,
                processedKeyCount = 0,
                appliedGrowth = 0,
                plantedCount = 0,
                harvestedCount = 0,
                newlyReadyCount = 0,
                harvestedPlotIds = emptyList(),
                lastKeyLabel = null,
                message = null,
            )
        }

        var working = state
        var appliedGrowth = 0
        var plantedCount = 0
        var harvestedCount = 0
        var newlyReadyCount = 0
        val harvestedPlotIds = mutableListOf<String>()
        var lastKeyLabel: String? = null
        var lastMessage: String? = null

        insertedTokens.forEach { token ->
            val keyLabel = normalizeKeyLabel(token)
            val target = keyLabel?.let { plotForKey(working, it) } ?: fallbackPlot(working)
            if (target != null) {
                val before = target
                if (before.status == PlotStatus.READY && before.id in protectedReadyPlotIds) {
                    lastMessage = "先在工坊完成这篇日记，再收获它的记忆作物"
                    lastKeyLabel = keyLabel ?: "中"
                    return@forEach
                }
                val result = interactWithPlot(working, target.id, growthMultiplier)
                working = result.state
                val after = working.plots.first { it.id == target.id }
                when {
                    before.status == PlotStatus.EMPTY && after.status == PlotStatus.GROWING -> plantedCount += 1
                    before.status == PlotStatus.READY && after.status == PlotStatus.EMPTY -> {
                        harvestedCount += 1
                        harvestedPlotIds += before.id
                    }
                    else -> appliedGrowth += (after.growth - before.growth).coerceAtLeast(0)
                }
                if (before.status != PlotStatus.READY && after.status == PlotStatus.READY) newlyReadyCount += 1
                lastMessage = result.message
                lastKeyLabel = keyLabel ?: "中"
            }
        }

        val insertedCharacters = insertedTokens.count { token -> token.any { !it.isWhitespace() } }
        working = working.copy(typedCharacterTotal = working.typedCharacterTotal + insertedCharacters)
        val summary =
            buildList {
                if (plantedCount > 0) add("播种 $plantedCount")
                if (appliedGrowth > 0) add("成长 +$appliedGrowth")
                if (newlyReadyCount > 0) add("成熟 $newlyReadyCount")
                if (harvestedCount > 0) add("收获 $harvestedCount")
            }.joinToString(" · ")
        return TypingGrowthResult(
            state = working,
            insertedCharacters = insertedCharacters,
            processedKeyCount = insertedTokens.size,
            appliedGrowth = appliedGrowth,
            plantedCount = plantedCount,
            harvestedCount = harvestedCount,
            newlyReadyCount = newlyReadyCount,
            harvestedPlotIds = harvestedPlotIds,
            lastKeyLabel = lastKeyLabel,
            message = summary.ifBlank { lastMessage.orEmpty() }.ifBlank { null },
        )
    }

    private fun plotForKey(state: FarmGameState, keyLabel: String): PlotState? {
        val index = keyLabels.indexOf(keyLabel)
        return if (index in state.plots.indices) state.plots[index] else null
    }

    private fun fallbackPlot(state: FarmGameState): PlotState? =
        state.plots
            .filter { it.type == PlotType.SOIL && it.status == PlotStatus.GROWING }
            .minByOrNull(::growthRatio)
            ?: state.plots.firstOrNull { it.type == PlotType.SOIL && it.status == PlotStatus.EMPTY }
            ?: state.plots.firstOrNull { it.type == PlotType.SOIL && it.status == PlotStatus.READY }

    private fun growthRatio(plot: PlotState): Float {
        val needed = plot.contentId?.let(FarmCatalog::find)?.growthNeeded ?: return Float.MAX_VALUE
        return if (needed == 0) Float.MAX_VALUE else plot.growth.toFloat() / needed
    }

    private fun normalizeKeyLabel(token: String): String? =
        when (token) {
            " " -> "SPACE"
            "\n", "\r" -> "ENTER"
            "，" -> ","
            "。" -> "."
            "、" -> "/"
            "；" -> ";"
            "’", "‘" -> "'"
            else -> token.uppercase().takeIf { it in keyLabels }
        }

    private fun insertedText(previous: String, next: String): String {
        val prefixLength = previous.zip(next).takeWhile { (old, new) -> old == new }.size
        val previousTail = previous.drop(prefixLength)
        val nextTail = next.drop(prefixLength)
        val suffixLength = previousTail.reversed().zip(nextTail.reversed()).takeWhile { (old, new) -> old == new }.size
        return next.substring(prefixLength, next.length - suffixLength)
    }

    private fun String.toCodePointStrings(): List<String> =
        codePoints().toArray().map { codePoint -> String(Character.toChars(codePoint)) }
}
