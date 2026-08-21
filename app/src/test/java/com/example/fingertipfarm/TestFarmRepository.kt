package com.example.fingertipfarm

import com.example.fingertipfarm.data.repository.FarmRepository
import com.example.fingertipfarm.domain.model.FarmGameState

class TestFarmRepository(initial: FarmGameState) : FarmRepository {
    private var stored = initial
    var saveCount: Int = 0
        private set

    override fun load(): FarmGameState = stored

    override fun save(state: FarmGameState) {
        stored = state
        saveCount += 1
    }

    fun current(): FarmGameState = stored
}
