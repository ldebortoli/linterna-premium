package com.linternapremium.app.ports

internal enum class StaticPcmState { UNINITIALIZED, EMPTY, READY }

internal interface StaticPcmTrack {
    val state: StaticPcmState
    fun write(samples: ShortArray): Int
    fun play()
}
