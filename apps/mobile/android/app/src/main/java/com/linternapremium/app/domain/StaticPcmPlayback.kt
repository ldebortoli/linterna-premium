package com.linternapremium.app.domain

import com.linternapremium.app.ports.StaticPcmState
import com.linternapremium.app.ports.StaticPcmTrack

internal fun startStaticPcmPlayback(track: StaticPcmTrack, samples: ShortArray) {
    require(samples.isNotEmpty())
    // MODE_STATIC is allocated but EMPTY until its first write, not READY.
    check(track.state != StaticPcmState.UNINITIALIZED) { "AudioTrack allocation failed" }
    val written = track.write(samples)
    check(written == samples.size) { "AudioTrack wrote $written of ${samples.size} samples" }
    check(track.state == StaticPcmState.READY) { "AudioTrack not ready after PCM upload" }
    track.play()
}
