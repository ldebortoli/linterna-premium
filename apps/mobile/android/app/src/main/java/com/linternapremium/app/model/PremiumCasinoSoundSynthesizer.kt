package com.linternapremium.app.model

import kotlin.math.PI
import kotlin.math.sin

internal object PremiumCasinoSoundSynthesizer {
    const val SAMPLE_RATE = 22_050

    fun synthesize(): ShortArray {
        val sampleCount = (SAMPLE_RATE * PREMIUM_SOUND_DURATION_MILLIS / 1_000L).toInt()
        val mix = FloatArray(sampleCount)

        repeat(PREMIUM_SOUND_ROUND_COUNT) { round ->
            val roundStart = round * PREMIUM_SOUND_ROUND_DURATION_MILLIS / 1_000f
            repeat(PREMIUM_REEL_TONES_PER_ROUND) { reel ->
                addReelClick(mix, roundStart + reel * 0.26f, round * 17 + reel)
            }
            repeat(PREMIUM_COIN_TONES_PER_ROUND) { coin ->
                addCoin(
                    samples = mix,
                    startSeconds = roundStart + 1.55f + coin * 0.075f,
                    frequency = 1_180f + coin * 72f,
                )
            }
            addTone(mix, roundStart + 2.16f, 0.20f, 523.25f, 0.26f)
            addTone(mix, roundStart + 2.34f, 0.20f, 659.25f, 0.28f)
            addTone(mix, roundStart + 2.52f, 0.20f, 783.99f, 0.30f)
            addJackpotChord(mix, roundStart + 2.70f)
        }

        return ShortArray(sampleCount) { index ->
            (mix[index].coerceIn(-1f, 1f) * Short.MAX_VALUE * 0.78f).toInt().toShort()
        }
    }

    private fun addReelClick(samples: FloatArray, startSeconds: Float, seed: Int) {
        val start = (startSeconds * SAMPLE_RATE).toInt()
        val count = (0.065f * SAMPLE_RATE).toInt()
        var noiseState = 0x13579BDF + seed * 97
        repeat(count) { offset ->
            val index = start + offset
            if (index !in samples.indices) return@repeat
            val progress = offset.toFloat() / count
            val envelope = (1f - progress) * (1f - progress)
            noiseState = noiseState * 1_664_525 + 1_013_904_223
            val noise = ((noiseState ushr 8) and 0xFFFF) / 32_767.5f - 1f
            val snap = sin(2.0 * PI * 1_520.0 * offset / SAMPLE_RATE).toFloat()
            samples[index] += (noise * 0.24f + snap * 0.18f) * envelope
        }
    }

    private fun addCoin(samples: FloatArray, startSeconds: Float, frequency: Float) {
        addTone(samples, startSeconds, 0.13f, frequency, 0.17f)
        addTone(samples, startSeconds + 0.018f, 0.10f, frequency * 1.51f, 0.10f)
    }

    private fun addJackpotChord(samples: FloatArray, startSeconds: Float) {
        addTone(samples, startSeconds, 0.28f, 523.25f, 0.24f)
        addTone(samples, startSeconds, 0.28f, 659.25f, 0.22f)
        addTone(samples, startSeconds, 0.28f, 783.99f, 0.22f)
        addTone(samples, startSeconds + 0.08f, 0.20f, 1_046.50f, 0.18f)
    }

    private fun addTone(
        samples: FloatArray,
        startSeconds: Float,
        durationSeconds: Float,
        frequency: Float,
        amplitude: Float,
    ) {
        val start = (startSeconds * SAMPLE_RATE).toInt()
        val count = (durationSeconds * SAMPLE_RATE).toInt()
        repeat(count) { offset ->
            val index = start + offset
            if (index !in samples.indices) return@repeat
            val progress = offset.toFloat() / count
            val envelope = (1f - progress) * (1f - progress)
            val fundamental = sin(2.0 * PI * frequency * offset / SAMPLE_RATE).toFloat()
            val harmonic = sin(2.0 * PI * frequency * 2.01f * offset / SAMPLE_RATE).toFloat()
            samples[index] += (fundamental + harmonic * 0.24f) * envelope * amplitude
        }
    }
}

