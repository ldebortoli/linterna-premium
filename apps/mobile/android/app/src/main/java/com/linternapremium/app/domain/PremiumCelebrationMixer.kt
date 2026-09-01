package com.linternapremium.app.domain

import com.linternapremium.app.model.PREMIUM_REAL_SOUND_CUES
import com.linternapremium.app.model.PREMIUM_REAL_SOUND_FADE_IN_MILLIS
import com.linternapremium.app.model.PREMIUM_REAL_SOUND_FADE_OUT_MILLIS
import com.linternapremium.app.model.PREMIUM_SOUND_DURATION_MILLIS
import com.linternapremium.app.model.PREMIUM_SOUND_ROUND_DURATION_MILLIS
import com.linternapremium.app.model.PREMIUM_VICTORY_START_IN_ROUND_MILLIS
import com.linternapremium.app.model.PremiumCasinoSoundSynthesizer

internal object PremiumCelebrationMixer {
    private const val RATE = PremiumCasinoSoundSynthesizer.SAMPLE_RATE

    fun decodePcm(bytes: ByteArray): ShortArray {
        require(bytes.size % 2 == 0) { "Truncated PCM sample" }
        return ShortArray(bytes.size / 2) { index ->
            ((bytes[index * 2].toInt() and 0xff) or (bytes[index * 2 + 1].toInt() shl 8)).toShort()
        }
    }

    fun mix(victory: ShortArray, clips: Map<Int, ShortArray>): ShortArray {
        require(victory.size.toLong() == RATE * PREMIUM_SOUND_DURATION_MILLIS / 1000L)
        val mix = FloatArray(victory.size) { index ->
            val millisInRound = index * 1000L / RATE % PREMIUM_SOUND_ROUND_DURATION_MILLIS
            if (millisInRound >= PREMIUM_VICTORY_START_IN_ROUND_MILLIS) {
                val attack = ((millisInRound - PREMIUM_VICTORY_START_IN_ROUND_MILLIS) / 20f).coerceIn(0f, 1f)
                val release = ((PREMIUM_SOUND_ROUND_DURATION_MILLIS - 1 - millisInRound) / 30f).coerceIn(0f, 1f)
                victory[index] * 0.80f * attack * release
            } else 0f
        }
        val fadeIn = RATE * PREMIUM_REAL_SOUND_FADE_IN_MILLIS / 1000
        val fadeOut = RATE * PREMIUM_REAL_SOUND_FADE_OUT_MILLIS / 1000
        PREMIUM_REAL_SOUND_CUES.forEach { cue ->
            val clip = clips[cue.assetId] ?: return@forEach
            val start = RATE * cue.startMillis / 1000
            val count = minOf(clip.size, RATE * cue.durationMillis / 1000)
            repeat(count) { offset ->
                val envelope = minOf(offset.toFloat() / fadeIn, (count - 1 - offset).toFloat() / fadeOut, 1f)
                mix[start + offset] += clip[offset] * cue.gain * envelope
            }
        }
        return ShortArray(mix.size) { mix[it].coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort() }
    }
}
