package com.linternapremium.app.domain

import com.linternapremium.app.model.CelebrationSoundCue
import com.linternapremium.app.model.CelebrationSoundKind
import com.linternapremium.app.model.PREMIUM_APPROVED_CLIP_SAMPLES
import com.linternapremium.app.model.PREMIUM_REAL_SOUND_CUES
import com.linternapremium.app.model.PREMIUM_SYNTHETIC_GAIN
import com.linternapremium.app.model.PremiumCasinoSoundSynthesizer
import org.junit.Assert.*
import org.junit.Test

class PremiumCelebrationMixerTest {
    private val rate = PremiumCasinoSoundSynthesizer.SAMPLE_RATE
    private fun frame(millis: Int) = rate * millis / 1000
    private fun clips(value: Short = 20_000) = PREMIUM_APPROVED_CLIP_SAMPLES.mapValues { (_, samples) ->
        ShortArray(samples) { value }
    }
    private fun startFrame(cue: CelebrationSoundCue) = frame(cue.startMillis)
    private fun endFrame(cue: CelebrationSoundCue) = startFrame(cue) + PREMIUM_APPROVED_CLIP_SAMPLES.getValue(cue.assetId)

    @Test
    fun `all seven approved full clips form a continuous chain without competing prizes`() {
        assertEquals(setOf(531, 459, 437, 2012, 2011, 1934, 1928), PREMIUM_REAL_SOUND_CUES.map { it.assetId }.toSet())
        assertFalse(PREMIUM_REAL_SOUND_CUES.any { it.assetId == 462 })
        assertEquals(3, PREMIUM_REAL_SOUND_CUES.count { it.assetId == 2011 })

        var coveredUntil = 0
        PREMIUM_REAL_SOUND_CUES.sortedBy(::startFrame).forEach { cue ->
            assertTrue("Gap before ${cue.assetId}", startFrame(cue) <= coveredUntil)
            coveredUntil = maxOf(coveredUntil, endFrame(cue))
        }
        assertTrue(coveredUntil >= frame(14_990))

        for (millis in 0 until 15_000) {
            val at = frame(millis)
            val playing = PREMIUM_REAL_SOUND_CUES.filter { at in startFrame(it) until endFrame(it) }
            assertTrue(playing.count { it.kind == CelebrationSoundKind.PRIZE } <= 1)
            assertTrue(playing.count { it.kind == CelebrationSoundKind.PEOPLE } <= 2)
            if (millis < 14_990) assertTrue("No real sound at $millis ms", playing.isNotEmpty())
        }
    }

    @Test
    fun `every occurrence starts at source zero and finishes naturally`() {
        val mixed = PremiumCelebrationMixer.mix(ShortArray(rate * 15), clips())
        PREMIUM_REAL_SOUND_CUES.forEach { cue ->
            assertTrue("Missing first sample for ${cue.assetId}", mixed[startFrame(cue)] > 0)
            assertTrue("Missing final sample for ${cue.assetId}", mixed[endFrame(cue) - 1] > 0)
            assertTrue("Interrupted sound ${cue.assetId}", endFrame(cue) <= mixed.size)
        }
    }

    @Test
    fun `continuous synthetic bed preserves fifteen seconds without silent samples`() {
        val original = ShortArray(rate * 15) { 10_000 }
        val mixed = PremiumCelebrationMixer.mix(original, emptyMap())
        assertEquals(rate * 15, mixed.size)
        assertEquals((10_000 * PREMIUM_SYNTHETIC_GAIN).toInt().toShort(), mixed.first())
        assertTrue(mixed.all { it != 0.toShort() })
    }

    @Test
    fun `overlaps remain bounded instead of clipping`() {
        val mixed = PremiumCelebrationMixer.mix(ShortArray(rate * 15) { 10_000 }, clips())
        assertTrue(mixed.any { it > 10_000 })
        assertTrue(mixed.all { it in (Short.MIN_VALUE + 1)..(Short.MAX_VALUE - 1) })
    }

    @Test
    fun `missing clips preserve the complete original track`() {
        val original = ShortArray(rate * 15) { -1000 }
        val mixed = PremiumCelebrationMixer.mix(original, emptyMap())
        assertEquals(rate * 15, mixed.size)
        assertTrue(mixed.all { it == (-300).toShort() })
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

    @Test(expected = IllegalArgumentException::class)
    fun `reject an incomplete approved clip`() {
        PremiumCelebrationMixer.mix(
            ShortArray(rate * 15),
            mapOf(1934 to ShortArray(PREMIUM_APPROVED_CLIP_SAMPLES.getValue(1934) - 1)),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reject a cue that would interrupt a full sound at second fifteen`() {
        val cue = CelebrationSoundCue(1934, 14_999, 0.24f, CelebrationSoundKind.PRIZE)
        PremiumCelebrationMixer.mix(ShortArray(rate * 15), clips(), listOf(cue))
    }
}
