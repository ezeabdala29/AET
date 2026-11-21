package com.aether.player.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.aether.player.model.AudioFolder
import com.aether.player.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AudioScanner(private val context: Context) {

    suspend fun scanTracks(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.IS_MUSIC
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Title"
                    val artist = cursor.getString(artistCol).takeIf { it != "<unknown>" } ?: "Unknown Artist"
                    val album = cursor.getString(albumCol).takeIf { it != "<unknown>" } ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val dataPath = cursor.getString(dataCol) ?: ""
                    val size = cursor.getLong(sizeCol)
                    val mime = cursor.getString(mimeCol) ?: "audio/*"

                    val file = File(dataPath)
                    val folderPath = file.parent ?: ""
                    val folderName = file.parentFile?.name ?: "Root"

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    // Optional audio extraction for sample rate & bitrate
                    var sampleRate = 0
                    var bitRate = 0L
                    var bitDepth = 16 // Standard default, FLAC/WAV may be 24

                    if (file.exists() && file.canRead()) {
                        try {
                            MediaMetadataRetriever().use { mmr ->
                                mmr.setDataSource(file.absolutePath)
                                val srStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                                val brStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                                sampleRate = srStr?.toIntOrNull() ?: 0
                                bitRate = brStr?.toLongOrNull() ?: 0L
                                
                                val ext = file.extension.lowercase()
                                if (ext == "flac" || ext == "wav" || ext == "alac") {
                                    // Check if high res
                                    if (bitRate > 1411200L || sampleRate > 48000) {
                                        bitDepth = 24
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    tracks.add(
                        Track(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            uri = contentUri,
                            dataPath = dataPath,
                            folderPath = folderPath,
                            folderName = folderName,
                            sizeBytes = size,
                            mimeType = mime,
                            sampleRate = sampleRate,
                            bitDepth = bitDepth,
                            bitRate = bitRate
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tracks
    }

    fun groupFolders(tracks: List<Track>): List<AudioFolder> {
        return tracks
            .groupBy { it.folderPath }
            .map { (path, list) ->
                AudioFolder(
                    path = path,
                    name = list.firstOrNull()?.folderName ?: File(path).name,
                    trackCount = list.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}
