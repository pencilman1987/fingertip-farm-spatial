package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.CatalogItem
import com.example.fingertipfarm.domain.model.CatalogKind
import com.example.fingertipfarm.domain.model.FarmGameState

data class PurchaseResult(
    val state: FarmGameState,
    val message: String,
    val purchased: Boolean,
)

class PurchaseItemUseCase {
    operator fun invoke(state: FarmGameState, item: CatalogItem): PurchaseResult {
        if (state.owns(item)) {
            return PurchaseResult(state, "${item.name}已经拥有", purchased = false)
        }
        if (state.gold < item.price) {
            return PurchaseResult(state, "金币不足，还需要 ${item.price - state.gold}G", purchased = false)
        }

        val paid = state.copy(gold = state.gold - item.price)
        val updated =
            when (item.kind) {
                CatalogKind.CROP -> paid.copy(unlockedCropIds = paid.unlockedCropIds + item.id)
                CatalogKind.FISH -> paid.copy(unlockedFishIds = paid.unlockedFishIds + item.id)
                CatalogKind.PARTNER -> paid.copy(ownedPartnerIds = paid.ownedPartnerIds + item.id)
            }
        return PurchaseResult(updated, "已获得 ${item.name}", purchased = true)
    }

    private fun FarmGameState.owns(item: CatalogItem): Boolean =
        when (item.kind) {
            CatalogKind.CROP -> item.id in unlockedCropIds
            CatalogKind.FISH -> item.id in unlockedFishIds
            CatalogKind.PARTNER -> item.id in ownedPartnerIds
        }
}
