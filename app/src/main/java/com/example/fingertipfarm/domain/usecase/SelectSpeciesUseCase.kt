package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.CatalogKind
import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmGameState

data class SpeciesSelectionResult(
    val state: FarmGameState,
    val message: String,
    val selected: Boolean,
)

class SelectSpeciesUseCase {
    operator fun invoke(state: FarmGameState, itemId: String): SpeciesSelectionResult {
        val item = FarmCatalog.find(itemId)
            ?: return SpeciesSelectionResult(state, "未找到该品种", selected = false)
        val isUnlocked =
            when (item.kind) {
                CatalogKind.CROP -> item.id in state.unlockedCropIds
                CatalogKind.FISH -> item.id in state.unlockedFishIds
                CatalogKind.PARTNER -> false
            }
        if (!isUnlocked) {
            return SpeciesSelectionResult(state, "该品种尚未解锁", selected = false)
        }

        val updated =
            when (item.kind) {
                CatalogKind.CROP -> state.copy(activeCropId = item.id)
                CatalogKind.FISH -> state.copy(activeFishId = item.id)
                CatalogKind.PARTNER -> state
            }
        return SpeciesSelectionResult(updated, "已选择 ${item.name}", selected = true)
    }
}
