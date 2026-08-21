package com.example.fingertipfarm.domain.usecase

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DailyPromptUseCaseTest {
    private val useCase = DailyPromptUseCase()

    @Test
    fun sameDateAlwaysReturnsSameQuestion() {
        val date = LocalDate.of(2026, 8, 4)
        assertEquals(useCase(date), useCase(date))
    }

    @Test
    fun adjacentDatesRotateQuestion() {
        val date = LocalDate.of(2026, 8, 4)
        assertNotEquals(useCase(date), useCase(date.plusDays(1)))
    }
}
