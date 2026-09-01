package com.linternapremium.app.domain

import com.linternapremium.app.model.CelebrationSoundKind
import com.linternapremium.app.model.PREMIUM_REAL_SOUND_CUES
import com.linternapremium.app.model.PREMIUM_VICTORY_START_IN_ROUND_MILLIS
import com.linternapremium.app.model.PremiumCasinoSoundSynthesizer
import org.junit.Assert.*
import org.junit.Test

class PremiumCelebrationMixerTest {
    private val rate = PremiumCasinoSoundSynthesizer.SAMPLE_RATE
    private fun frame(millis: Int) = rate * millis / 1000
    private fun clips(value: Short = 20_000) = PREMIUM_REAL_SOUND_CUES.associate { cue ->
        cue.assetId to ShortArray(frame(cue.durationMillis)) { value }
    }

    @Test
    fun `all seven approved clips are scheduled without rejected crowd or competing prizes`() {
        assertEquals(setOf(531, 459, 437, 2012, 2011, 1934, 1928), PREMIUM_REAL_SOUND_CUES.map { it.assetId }.toSet())
        for (millis in 0 until 15_000) {
            val playing = PREMIUM_REAL_SOUND_CUES.filter { millis in it.startMillis until it.startMillis + it.durationMillis }
            assertTrue(playing.count { it.kind == CelebrationSoundKind.PRIZE } <= 1)
            assertTrue(playing.count { it.kind == CelebrationSoundKind.PEOPLE } <= 1)
            if (millis % 3000 >= PREMIUM_VICTORY_START_IN_ROUND_MILLIS) assertTrue(playing.isEmpty())
        }
    }

    @Test
    fun `each approved voice or prize contributes audible samples`() {
        clips().forEach { (id, clip) ->
            val mixed = PremiumCelebrationMixer.mix(ShortArray(rate * 15), mapOf(id to clip))
            assertTrue("Missing sound $id", mixed.any { it > 2000 })
        }
    }

    @Test
    fun `mix stays fifteen seconds with five real breaks and original final fanfare`() {
        val original = PremiumCasinoSoundSynthesizer.synthesize()
        val mixed = PremiumCelebrationMixer.mix(original, clips())
        assertEquals(rate * 15, mixed.size)
        listOf(1500..1900, 3050..3300, 6050..6350, 9050..9250, 12050..12350).forEach { rest ->
            assertTrue(mixed.sliceArray(frame(rest.first)..frame(rest.last)).all { it == 0.toShort() })
        }
        assertTrue(mixed.takeLast(rate / 10).any { kotlin.math.abs(it.toInt()) > 500 })
        assertTrue(mixed.all { kotlin.math.abs(it.toInt()) < 30_000 })
    }

    @Test
    fun `real clips have gentle attacks and fades to silence`() {
        val mixed = PremiumCelebrationMixer.mix(ShortArray(rate * 15), clips())
        assertEquals(0, mixed[0].toInt())
        assertTrue(mixed[frame(10)] in 1..4000)
        assertTrue(mixed[frame(100)] > mixed[frame(10)])
        assertTrue(mixed[frame(1330)] < mixed[frame(1200)])
        assertEquals(0, mixed[frame(1350) - 1].toInt())
    }

    @Test
    fun `short empty and missing clips do not overrun or change duration`() {
        val mixed = PremiumCelebrationMixer.mix(ShortArray(rate * 15), mapOf(1934 to shortArrayOf(-1000, -1000), 531 to shortArrayOf()))
        assertEquals(rate * 15, mixed.size)
        assertTrue(mixed.all { it == 0.toShort() })
        val negative = PremiumCelebrationMixer.mix(ShortArray(rate * 15), clips(Short.MIN_VALUE))
        assertTrue(negative.any { it < -2000 })
        assertTrue(negative.all { it > Short.MIN_VALUE })
    }

    @Test
    fun `decoder preserves little endian signed samples and empty data`() {
        assertArrayEquals(shortArrayOf(0, 32767, -32768, -1, 255), PremiumCelebrationMixer.decodePcm(byteArrayOf(0, 0, -1, 127, 0, -128, -1, -1, -1, 0)))
        assertArrayEquals(shortArrayOf(), PremiumCelebrationMixer.decodePcm(byteArrayOf()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reject truncated PCM byte`() { PremiumCelebrationMixer.decodePcm(byteArrayOf(1)) }

    @Test(expected = IllegalArgumentException::class)
    fun `reject a victory track with wrong duration`() { PremiumCelebrationMixer.mix(shortArrayOf(1), emptyMap()) }
}
