package com.aether.player.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackMetadataTest {
    @Test
    fun unknownSourceFormatIsNotInventedFromExtension() {
        assertEquals(
            "FLAC · formato de salida no confirmado",
            AudioFormatInfo.describe("/music/track.flac", sampleRate = 0, bitDepth = 0)
        )
    }

    @Test
    fun measuredSourceFormatIsShownWhenAvailable() {
        assertEquals(
            "WAV · 24-bit / 96.0 kHz",
            AudioFormatInfo.describe("/music/track.wav", sampleRate = 96_000, bitDepth = 24)
        )
    }
}
