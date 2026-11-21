package com.aether.player.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.aether.player.MainActivity
import com.aether.player.model.AudiophileSettings
import com.aether.player.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
class AudioPlayerEngine(private val context: Context) {

    var exoPlayer: ExoPlayer? = null
        private set

    var mediaSession: MediaSession? = null
        private set
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _settings = MutableStateFlow(AudiophileSettings())
    val settings: StateFlow<AudiophileSettings> = _settings.asStateFlow()

    init {
        buildPlayer()
    }

    private fun buildPlayer() {
        mediaSession?.release()
        exoPlayer?.release()

        // Bit-Perfect AudioAttributes: ContentType MUSIC + USAGE_MEDIA
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_NONE) // Audio purist: prevent background capture/mixing
            .build()

        // Pure standard renderers factory that avoids FloatOutput timestamp discontinuity on FLAC
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        exoPlayer = ExoPlayer.Builder(context, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build().apply {
                playbackParameters = PlaybackParameters.DEFAULT // 1.0f speed, 1.0f pitch, strictly pristine
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            _isPlaying.value = false
                        }
                    }
                })
            }

        // Initialize MediaSession for lock screen & notification controls
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(context, exoPlayer!!)
            .setSessionActivity(pendingIntent)
            .build()
    }

    fun playTrack(track: Track) {
        _currentTrack.value = track
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.albumArtUri)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(track.uri)
            .setMediaMetadata(mediaMetadata)
            .build()

        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun updateSettings(newSettings: AudiophileSettings) {
        _settings.value = newSettings
        buildPlayer()
        _currentTrack.value?.let { playTrack(it) }
    }

    fun release() {
        mediaSession?.release()
        exoPlayer?.release()
        mediaSession = null
        exoPlayer = null
    }
}
