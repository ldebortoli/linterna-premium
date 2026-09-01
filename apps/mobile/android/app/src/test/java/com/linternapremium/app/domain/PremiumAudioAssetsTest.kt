package com.linternapremium.app.domain

import com.linternapremium.app.model.PREMIUM_REAL_SOUND_CUES
import com.linternapremium.app.model.PREMIUM_APPROVED_CLIP_SAMPLES
import com.linternapremium.app.model.PremiumCasinoSoundSynthesizer
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

class PremiumAudioAssetsTest {
    @Test
    fun `prepared licensed clips produce a bounded audible fifteen second mix`() {
        val directory = File("src/main/assets/premium-sfx")
        // Public CI deliberately has no licensed source assets. Run audio:prepare for local integration QA.
        assumeTrue("Licensed clips are an opt-in local integration suite", directory.isDirectory)
        val clips = PREMIUM_REAL_SOUND_CUES.associate { cue ->
            val pcm = PremiumCelebrationMixer.decodePcm(File(directory, "${cue.assetId}.pcm").readBytes())
            assertEquals(PREMIUM_APPROVED_CLIP_SAMPLES.getValue(cue.assetId), pcm.size)
            assertTrue(pcm.any { kotlin.math.abs(it.toInt()) > 500 })
            cue.assetId to pcm
        }
        val mixed = PremiumCelebrationMixer.mix(PremiumCasinoSoundSynthesizer.synthesize(), clips)
        assertEquals(330750, mixed.size)
        assertTrue(mixed.none { it == Short.MIN_VALUE || it == Short.MAX_VALUE })
        PREMIUM_REAL_SOUND_CUES.forEach { cue ->
            val start = 22050 * cue.startMillis / 1000
            val region = mixed.sliceArray(start until start + PREMIUM_APPROVED_CLIP_SAMPLES.getValue(cue.assetId))
            assertTrue(region.any { kotlin.math.abs(it.toInt()) > 500 })
        }
        val windowSize = 22050 / 50
        mixed.asList().chunked(windowSize).forEachIndexed { index, window ->
            val peak = window.maxOf { kotlin.math.abs(it.toInt()) }
            assertTrue("Audible gap near ${index * 20} ms", peak > 250)
        }
        val wav = ByteBuffer.allocate(44 + mixed.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        wav.put("RIFF".toByteArray()).putInt(36 + mixed.size * 2).put("WAVEfmt ".toByteArray())
        wav.putInt(16).putShort(1).putShort(1).putInt(22050).putInt(44100).putShort(2).putShort(16)
        wav.put("data".toByteArray()).putInt(mixed.size * 2)
        mixed.forEach { wav.putShort(it) }
        val preview = File("build/reports/audio/premium-celebration-preview.wav")
        preview.parentFile?.mkdirs()
        preview.writeBytes(wav.array())
    }
}
