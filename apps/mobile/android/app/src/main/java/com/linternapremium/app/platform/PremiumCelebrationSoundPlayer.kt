package com.linternapremium.app.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.SystemClock
import android.util.Log
import com.linternapremium.app.domain.PremiumCelebrationMixer
import com.linternapremium.app.domain.startStaticPcmPlayback
import com.linternapremium.app.model.PREMIUM_REAL_SOUND_CUES
import com.linternapremium.app.model.PREMIUM_SOUND_DURATION_MILLIS
import com.linternapremium.app.model.PremiumCasinoSoundSynthesizer
import com.linternapremium.app.ports.StaticPcmState
import com.linternapremium.app.ports.StaticPcmTrack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PremiumCelebrationSoundPlayer(context: Context) {
    private val assets = context.applicationContext.assets
    private val preparation = Mutex()
    private var prepared: ShortArray? = null
    private var playbackJob: Job? = null

    // Warm once on opening the screen, so synthesis does not delay the celebration.
    suspend fun prepare(): ShortArray = preparation.withLock {
        prepared ?: withContext(Dispatchers.IO) {
            val victory = PremiumCasinoSoundSynthesizer.synthesize()
            val mixed = try {
                val clips = PREMIUM_REAL_SOUND_CUES.map { it.assetId }.distinct().associateWith { id ->
                    assets.open("premium-sfx/$id.pcm").use { PremiumCelebrationMixer.decodePcm(it.readBytes()) }
                }
                PremiumCelebrationMixer.mix(victory, clips)
            } catch (error: Exception) {
                Log.e(TAG, "Real celebration clips unavailable; using original victory track", error)
                victory
            }
            mixed.also { prepared = it }
        }
    }

    suspend fun play() {
        val job = currentCoroutineContext()[Job]
        playbackJob = job
        val startedAt = SystemClock.elapsedRealtime()
        try {
            val pcm = prepare()
            currentCoroutineContext().ensureActive()
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            val skip = (elapsed * PremiumCasinoSoundSynthesizer.SAMPLE_RATE / 1000L).coerceAtMost(pcm.size.toLong()).toInt()
            if (skip == pcm.size) return
            val remaining = pcm.copyOfRange(skip, pcm.size)
            withContext(Dispatchers.IO) {
                val track = createTrack(remaining.size)
                try {
                    currentCoroutineContext().ensureActive()
                    track.setVolume(0.80f)
                    startStaticPcmPlayback(AndroidStaticPcmTrack(track), remaining)
                    Log.d(TAG, "Celebration playback started: ${remaining.size} samples")
                    delay((PREMIUM_SOUND_DURATION_MILLIS - (SystemClock.elapsedRealtime() - startedAt)).coerceAtLeast(0L))
                } finally {
                    runCatching { track.stop() }
                    track.release()
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e(TAG, "Premium celebration audio playback failed", error)
        } finally {
            if (playbackJob === job) playbackJob = null
        }
    }

    // Its owning coroutine releases the track, including cancellation during upload.
    fun close() { playbackJob?.cancel() }

    private fun createTrack(sampleCount: Int): AudioTrack = AudioTrack.Builder()
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        .setAudioFormat(AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(PremiumCasinoSoundSynthesizer.SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build())
        .setTransferMode(AudioTrack.MODE_STATIC)
        .setBufferSizeInBytes(sampleCount * Short.SIZE_BYTES)
        .build()

    private class AndroidStaticPcmTrack(private val track: AudioTrack) : StaticPcmTrack {
        override val state: StaticPcmState
            get() = when (track.state) {
                AudioTrack.STATE_UNINITIALIZED -> StaticPcmState.UNINITIALIZED
                AudioTrack.STATE_NO_STATIC_DATA -> StaticPcmState.EMPTY
                else -> StaticPcmState.READY
            }

        override fun write(samples: ShortArray) = track.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
        override fun play() = track.play()
    }

    private companion object { const val TAG = "PremiumCelebrationAudio" }
}
