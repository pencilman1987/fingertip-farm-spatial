package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmDefaults
import com.example.fingertipfarm.domain.model.FarmSeason
import com.example.fingertipfarm.domain.model.ReflectionGardenSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PartnerAbilityUseCaseTest {
    private val useCase = PartnerAbilityUseCase()
    private val reflection = ReflectionGardenSummary(FarmSeason.SUMMER, 2, 1, 1, 0, 0, 20, "温柔回顾")

    @Test
    fun noOwnedPartnersProducesNoVisibleAbility() {
        assertTrue(useCase(FarmDefaults.initialState(), reflection, "今天好吗？").isEmpty())
    }

    @Test
    fun ownedPartnersExposeTheirRealAbilities() {
        val state = FarmDefaults.initialState().copy(ownedPartnerIds = setOf("sheep", "cat", "pig", "llama"))
        val notes = useCase(state, reflection, "今天好吗？")
        assertEquals(listOf("sheep", "cat", "pig", "llama"), notes.map { it.partnerId })
        assertTrue(notes.first { it.partnerId == "sheep" }.message.contains("自动收获"))
        assertTrue(notes.first { it.partnerId == "cat" }.message.contains("自动钓起"))
        assertTrue(notes.first { it.partnerId == "pig" }.message.contains("今天好吗"))
    }
}
