package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmGameState
import com.example.fingertipfarm.domain.model.PartnerAbilityNote
import com.example.fingertipfarm.domain.model.ReflectionGardenSummary

class PartnerAbilityUseCase {
    operator fun invoke(
        state: FarmGameState,
        reflection: ReflectionGardenSummary,
        dailyPrompt: String,
    ): List<PartnerAbilityNote> =
        listOfNotNull(
            note(state, "sheep", "正在巡逻农田，每 3 秒自动收获一株成熟作物。"),
            note(state, "cat", "正在守候鱼塘，每 30 秒自动钓起一条鱼。"),
            note(state, "pig", "今天想问：$dailyPrompt"),
            note(state, "llama", "带来了季节贴纸，手账工具里可以随时更换。"),
        )

    private fun note(state: FarmGameState, partnerId: String, message: String): PartnerAbilityNote? {
        if (partnerId !in state.ownedPartnerIds) return null
        val partner = FarmCatalog.find(partnerId) ?: return null
        return PartnerAbilityNote(partnerId, partner.name, partner.emoji, message)
    }
}
