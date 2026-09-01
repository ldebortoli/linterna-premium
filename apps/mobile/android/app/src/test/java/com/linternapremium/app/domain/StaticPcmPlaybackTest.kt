package com.linternapremium.app.domain

import com.linternapremium.app.ports.StaticPcmState
import com.linternapremium.app.ports.StaticPcmTrack
import org.junit.Assert.*
import org.junit.Test

class StaticPcmPlaybackTest {
    private class Track(
        override var state: StaticPcmState = StaticPcmState.EMPTY,
        val afterWrite: StaticPcmState = StaticPcmState.READY,
        val written: Int = 3,
        val failOnPlay: Boolean = false,
    ) : StaticPcmTrack {
        val events = mutableListOf<String>()
        override fun write(samples: ShortArray): Int {
            assertArrayEquals(shortArrayOf(1, -2, 3), samples)
            events.add("write")
            state = afterWrite
            return written
        }
        override fun play() {
            check(state == StaticPcmState.READY)
            if (failOnPlay) error("Device rejected playback")
            events.add("play")
        }
    }
    private val pcm = shortArrayOf(1, -2, 3)

    @Test
    fun `fresh static track must be loaded before checking ready state`() {
        val track = Track()
        startStaticPcmPlayback(track, pcm)
        assertEquals(listOf("write", "play"), track.events)
    }

    @Test
    fun `already ready track accepts a complete upload`() {
        val track = Track(state = StaticPcmState.READY)
        startStaticPcmPlayback(track, pcm)
        assertEquals(listOf("write", "play"), track.events)
    }

    @Test
    fun `allocation failure does not write or play`() {
        val track = Track(state = StaticPcmState.UNINITIALIZED)
        assertThrows(IllegalStateException::class.java) { startStaticPcmPlayback(track, pcm) }
        assertTrue(track.events.isEmpty())
    }

    @Test
    fun `incomplete or failed writes never start the track`() {
        listOf(-3, 0, 2).forEach { count ->
            val track = Track(written = count)
            assertThrows(IllegalStateException::class.java) { startStaticPcmPlayback(track, pcm) }
            assertEquals(listOf("write"), track.events)
        }
    }

    @Test
    fun `track remaining empty after upload is reported`() {
        val track = Track(afterWrite = StaticPcmState.EMPTY)
        assertThrows(IllegalStateException::class.java) { startStaticPcmPlayback(track, pcm) }
        assertEquals(listOf("write"), track.events)
    }

    @Test
    fun `empty PCM never touches the device`() {
        val track = Track()
        assertThrows(IllegalArgumentException::class.java) { startStaticPcmPlayback(track, shortArrayOf()) }
        assertTrue(track.events.isEmpty())
    }

    @Test
    fun `play errors reach the owner for logging and cleanup`() {
        assertThrows(IllegalStateException::class.java) { startStaticPcmPlayback(Track(failOnPlay = true), pcm) }
    }
}
