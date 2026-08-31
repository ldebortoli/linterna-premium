package com.linternapremium.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PremiumCelebrationTimelineTest {
    @Test
    fun `celebration lasts fifteen seconds while torch visuals end after three`() {
        assertEquals(15_000, PREMIUM_CELEBRATION_DURATION_MILLIS)
        assertEquals(0f, premiumTorchProgressAt(0f), 0.001f)
        assertEquals(0.5f, premiumTorchProgressAt(0.1f), 0.001f)
        assertEquals(1f, premiumTorchProgressAt(0.2f), 0.001f)
        assertEquals(1f, premiumTorchProgressAt(1f), 0.001f)
    }

    @Test
    fun `celebration repeats five visual rounds and finishes on jackpot`() {
        assertEquals(0f, repeatingCelebrationProgressAt(0f), 0.001f)
        assertEquals(0.5f, repeatingCelebrationProgressAt(0.1f), 0.001f)
        assertEquals(0f, repeatingCelebrationProgressAt(0.2f), 0.001f)
        assertEquals(0.5f, repeatingCelebrationProgressAt(0.9f), 0.001f)
        assertEquals(1f, repeatingCelebrationProgressAt(1f), 0.001f)
    }
}
