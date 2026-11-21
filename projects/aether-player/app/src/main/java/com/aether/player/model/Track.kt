package com.aether.player.model

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: Uri,
    val dataPath: String,
    val folderPath: String,
    val folderName: String,
    val sizeBytes: Long,
    val mimeType: String,
    val sampleRate: Int = 0,
    val bitDepth: Int = 0,
    val bitRate: Long = 0L
) {
    val durationFormatted: String
        get() {
            val totalSec = durationMs / 1000
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val audioFormatInfo: String
        get() {
            val extension = dataPath.substringAfterLast('.', "").uppercase()
            return when {
                sampleRate > 0 && bitDepth > 0 -> "$extension · $bitDepth-bit / ${sampleRate / 1000.0} kHz"
                sampleRate > 0 -> "$extension · ${sampleRate / 1000.0} kHz"
                extension.isNotEmpty() -> extension
                else -> "AUDIO"
            }
        }
}

data class AudioFolder(
    val path: String,
    val name: String,
    val trackCount: Int
)

data class AudiophileSettings(
    val bitPerfectMode: Boolean = true,
    val bypassSystemFx: Boolean = true,
    val lowLatencyBuffer: Boolean = false,
    val highResolutionOutput: Boolean = true
)
