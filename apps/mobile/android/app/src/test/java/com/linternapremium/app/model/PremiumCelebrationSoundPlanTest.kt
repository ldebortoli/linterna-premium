package com.linternapremium.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PremiumCelebrationSoundPlanTest {
    @Test
    fun `casino sound plan spans the full fifteen second celebration`() {
        assertEquals(5, PREMIUM_SOUND_ROUND_COUNT)
        assertEquals(3_000L, PREMIUM_SOUND_ROUND_DURATION_MILLIS)
        assertEquals(15_000L, PREMIUM_SOUND_DURATION_MILLIS)
        assertEquals(6, PREMIUM_REEL_TONES_PER_ROUND)
        assertEquals(8, PREMIUM_COIN_TONES_PER_ROUND)
        assertEquals(4, PREMIUM_JACKPOT_CHIMES_PER_ROUND)
    }

    @Test
    fun `synthesized casino track is non silent and exactly fifteen seconds long`() {
        val pcm = PremiumCasinoSoundSynthesizer.synthesize()

        assertEquals(15 * PremiumCasinoSoundSynthesizer.SAMPLE_RATE, pcm.size)
        assertEquals(true, pcm.any { it.toInt() != 0 })
    }
}
