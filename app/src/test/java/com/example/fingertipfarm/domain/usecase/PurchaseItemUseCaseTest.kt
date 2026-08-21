package com.example.fingertipfarm.domain.usecase

import com.example.fingertipfarm.domain.model.FarmCatalog
import com.example.fingertipfarm.domain.model.FarmDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseItemUseCaseTest {
    private val useCase = PurchaseItemUseCase()

    @Test
    fun purchaseUnlockedCropDeductsGoldAndAddsOwnership() {
        val item = FarmCatalog.crops.first { it.id == "tomato" }
        val result = useCase(FarmDefaults.initialState().copy(gold = 200), item)

        assertTrue(result.purchased)
        assertTrue("tomato" in result.state.unlockedCropIds)
        assertEquals(100, result.state.gold)
    }

    @Test
    fun insufficientGoldDoesNotPurchase() {
        val item = FarmCatalog.fish.first { it.id == "shark" }
        val state = FarmDefaults.initialState().copy(gold = 100)
        val result = useCase(state, item)

        assertFalse(result.purchased)
        assertEquals(state, result.state)
    }

    @Test
    fun alreadyOwnedItemIsNotChargedTwice() {
        val carrot = FarmCatalog.crops.first { it.id == "carrot" }
        val state = FarmDefaults.initialState()
        val result = useCase(state, carrot)

        assertFalse(result.purchased)
        assertEquals(state.gold, result.state.gold)
    }
}
