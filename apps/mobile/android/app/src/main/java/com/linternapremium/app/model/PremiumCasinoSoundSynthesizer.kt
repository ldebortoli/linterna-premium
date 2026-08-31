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
            addVictoryFanfare(mix, roundStart, finalRound = round == PREMIUM_SOUND_ROUND_COUNT - 1)
            addCrowdCheer(mix, roundStart + 2.04f, round)
            addHurraChorus(mix, roundStart + 2.54f)
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

    private fun addVictoryFanfare(samples: FloatArray, roundStart: Float, finalRound: Boolean) {
        val boost = if (finalRound) 1.28f else 1f
        addTrumpetTone(samples, roundStart + 1.98f, 0.17f, 392.00f, 0.16f * boost)
        addTrumpetTone(samples, roundStart + 2.14f, 0.17f, 523.25f, 0.18f * boost)
        addTrumpetTone(samples, roundStart + 2.30f, 0.19f, 659.25f, 0.20f * boost)
        addTrumpetTone(samples, roundStart + 2.48f, 0.38f, 783.99f, 0.23f * boost)
        if (finalRound) {
            addTrumpetTone(samples, roundStart + 2.82f, 0.18f, 1_046.50f, 0.25f)
        }
    }

    private fun addTrumpetTone(
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
            val attack = (progress / 0.08f).coerceIn(0f, 1f)
            val release = ((1f - progress) / 0.18f).coerceIn(0f, 1f)
            val envelope = attack * release
            val vibrato = 1f + sin(2.0 * PI * 5.4 * offset / SAMPLE_RATE).toFloat() * 0.006f
            var brass = 0f
            repeat(5) { harmonic ->
                val partial = harmonic + 1
                brass += sin(
                    2.0 * PI * frequency * vibrato * partial * offset / SAMPLE_RATE,
                ).toFloat() / partial
            }
            samples[index] += brass * envelope * amplitude
        }
    }

    private fun addCrowdCheer(samples: FloatArray, startSeconds: Float, seed: Int) {
        val start = (startSeconds * SAMPLE_RATE).toInt()
        val count = (0.92f * SAMPLE_RATE).toInt()
        var noiseState = 0x2468ACE + seed * 131
        var filteredNoise = 0f
        repeat(count) { offset ->
            val index = start + offset
            if (index !in samples.indices) return@repeat
            val progress = offset.toFloat() / count
            noiseState = noiseState * 1_103_515_245 + 12_345
            val noise = ((noiseState ushr 9) and 0x7FFF) / 16_383.5f - 1f
            filteredNoise = filteredNoise * 0.86f + noise * 0.14f
            val swell = sin(PI * progress).toFloat().coerceAtLeast(0f)
            val applause = 0.68f + 0.32f * sin(2.0 * PI * 8.5 * offset / SAMPLE_RATE).toFloat()
            samples[index] += filteredNoise * swell * applause * 0.10f
        }
    }

    private fun addHurraChorus(samples: FloatArray, startSeconds: Float) {
        repeat(3) { voice ->
            val voiceStart = startSeconds + voice * 0.014f
            val fundamental = 164f + voice * 23f
            addVowel(samples, voiceStart, 0.14f, fundamental, 340f, 880f, 0.095f)
            addRolledR(samples, voiceStart + 0.115f, 0.09f, fundamental, voice)
            addVowel(samples, voiceStart + 0.18f, 0.22f, fundamental * 1.08f, 780f, 1_240f, 0.12f)
        }
    }

    private fun addVowel(
        samples: FloatArray,
        startSeconds: Float,
        durationSeconds: Float,
        fundamental: Float,
        firstFormant: Float,
        secondFormant: Float,
        amplitude: Float,
    ) {
        val start = (startSeconds * SAMPLE_RATE).toInt()
        val count = (durationSeconds * SAMPLE_RATE).toInt()
        repeat(count) { offset ->
            val index = start + offset
            if (index !in samples.indices) return@repeat
            val progress = offset.toFloat() / count
            val envelope = sin(PI * progress).toFloat().coerceAtLeast(0f)
            val voice = sin(2.0 * PI * fundamental * offset / SAMPLE_RATE).toFloat() * 0.55f +
                sin(2.0 * PI * fundamental * 2f * offset / SAMPLE_RATE).toFloat() * 0.23f +
                sin(2.0 * PI * firstFormant * offset / SAMPLE_RATE).toFloat() * 0.14f +
                sin(2.0 * PI * secondFormant * offset / SAMPLE_RATE).toFloat() * 0.08f
            samples[index] += voice * envelope * amplitude
        }
    }

    private fun addRolledR(
        samples: FloatArray,
        startSeconds: Float,
        durationSeconds: Float,
        fundamental: Float,
        seed: Int,
    ) {
        val start = (startSeconds * SAMPLE_RATE).toInt()
        val count = (durationSeconds * SAMPLE_RATE).toInt()
        var noiseState = 0x1020304 + seed * 53
        repeat(count) { offset ->
            val index = start + offset
            if (index !in samples.indices) return@repeat
            val progress = offset.toFloat() / count
            noiseState = noiseState * 1_664_525 + 1_013_904_223
            val noise = ((noiseState ushr 8) and 0xFFFF) / 32_767.5f - 1f
            val trill = (0.5f + 0.5f * sin(2.0 * PI * 28.0 * offset / SAMPLE_RATE).toFloat())
            val tone = sin(2.0 * PI * fundamental * offset / SAMPLE_RATE).toFloat()
            val envelope = sin(PI * progress).toFloat().coerceAtLeast(0f)
            samples[index] += (tone * 0.07f + noise * 0.04f) * trill * envelope
        }
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
