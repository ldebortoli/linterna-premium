package com.linternapremium.app.platform

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.linternapremium.app.model.PREMIUM_SOUND_DURATION_MILLIS
import com.linternapremium.app.model.PremiumCasinoSoundSynthesizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PremiumCelebrationSoundPlayer {
    private var audioTrack: AudioTrack? = null

    suspend fun play() {
        val pcm = withContext(Dispatchers.Default) {
            PremiumCasinoSoundSynthesizer.synthesize()
        }
        val track = withContext(Dispatchers.IO) { createTrack(pcm) } ?: return
        audioTrack = track
        try {
            runCatching {
                track.setVolume(0.72f)
                track.play()
            }.getOrElse { return }
            delay(PREMIUM_SOUND_DURATION_MILLIS)
        } finally {
            release(track)
        }
    }

    fun close() {
        audioTrack?.let(::release)
    }

    private fun createTrack(pcm: ShortArray): AudioTrack? = runCatching {
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(PremiumCasinoSoundSynthesizer.SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(format)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * Short.SIZE_BYTES)
            .build()
        if (track.state != AudioTrack.STATE_INITIALIZED) {
            track.release()
            return@runCatching null
        }
        val written = track.write(pcm, 0, pcm.size, AudioTrack.WRITE_BLOCKING)
        if (written != pcm.size) {
            track.release()
            return@runCatching null
        }
        track
    }.getOrNull()

    private fun release(track: AudioTrack) {
        if (audioTrack === track) audioTrack = null
        runCatching { track.stop() }
        runCatching { track.flush() }
        runCatching { track.release() }
    }
}
