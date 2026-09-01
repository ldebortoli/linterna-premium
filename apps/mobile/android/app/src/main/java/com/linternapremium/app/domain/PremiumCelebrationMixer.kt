package com.linternapremium.app.domain

import com.linternapremium.app.model.PREMIUM_APPROVED_CLIP_SAMPLES
import com.linternapremium.app.model.PREMIUM_REAL_SOUND_CUES
import com.linternapremium.app.model.PREMIUM_SOUND_DURATION_MILLIS
import com.linternapremium.app.model.PREMIUM_SYNTHETIC_GAIN
import com.linternapremium.app.model.CelebrationSoundCue
import com.linternapremium.app.model.PremiumCasinoSoundSynthesizer

internal object PremiumCelebrationMixer {
    private const val RATE = PremiumCasinoSoundSynthesizer.SAMPLE_RATE

    fun decodePcm(bytes: ByteArray): ShortArray {
        require(bytes.size % 2 == 0) { "Truncated PCM sample" }
        return ShortArray(bytes.size / 2) { index ->
            ((bytes[index * 2].toInt() and 0xff) or (bytes[index * 2 + 1].toInt() shl 8)).toShort()
        }
    }

    fun mix(
        victory: ShortArray,
        clips: Map<Int, ShortArray>,
        cues: List<CelebrationSoundCue> = PREMIUM_REAL_SOUND_CUES,
    ): ShortArray {
        require(victory.size.toLong() == RATE * PREMIUM_SOUND_DURATION_MILLIS / 1000L)
        val mix = FloatArray(victory.size) { victory[it] * PREMIUM_SYNTHETIC_GAIN }
        cues.forEach { cue ->
            val clip = clips[cue.assetId] ?: return@forEach
            require(clip.size == PREMIUM_APPROVED_CLIP_SAMPLES.getValue(cue.assetId)) {
                "Clip ${cue.assetId} must be complete"
            }
            val start = RATE * cue.startMillis / 1000
            require(start + clip.size <= mix.size) { "Clip ${cue.assetId} would be cut at second 15" }
            clip.indices.forEach { offset -> mix[start + offset] += clip[offset] * cue.gain }
        }
        return ShortArray(mix.size) {
            mix[it].coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort()
        }
    }
}
