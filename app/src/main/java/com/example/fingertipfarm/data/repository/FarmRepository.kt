package com.example.fingertipfarm.data.repository

import com.example.fingertipfarm.domain.model.FarmGameState

interface FarmRepository {
    fun load(): FarmGameState

    fun save(state: FarmGameState)
}
