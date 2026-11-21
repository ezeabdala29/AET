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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val progressScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null

    private val _settings = MutableStateFlow(AudiophileSettings())
    val settings: StateFlow<AudiophileSettings> = _settings.asStateFlow()

    init {
        buildPlayer()
    }

    private fun buildPlayer() {
        mediaSession?.release()
        exoPlayer?.release()
        _isPlaying.value = false

        // Standard media attributes do not bypass Android's output mixer or guarantee bit-perfect output.
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .setAllowedCapturePolicy(
                if (_settings.value.bypassSystemFx) C.ALLOW_CAPTURE_BY_NONE else C.ALLOW_CAPTURE_BY_ALL
            )
            .build()

        // Pure standard renderers factory that avoids FloatOutput timestamp discontinuity on FLAC
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableAudioFloatOutput(_settings.value.highResolutionOutput)

        exoPlayer = ExoPlayer.Builder(context, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build().apply {
                playbackParameters = PlaybackParameters.DEFAULT
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) observeProgress() else updateProgress()
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        _currentTrack.value = _queue.value.firstOrNull { it.id.toString() == mediaItem?.mediaId }
                        updateProgress()
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updateProgress()
                        if (playbackState == Player.STATE_ENDED) {
                            _isPlaying.value = false
                        }
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        _shuffleEnabled.value = shuffleModeEnabled
                    }

                    override fun onRepeatModeChanged(repeatMode: Int) {
                        _repeatMode.value = repeatMode
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
        playTracks(listOf(track), 0)
    }

    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        loadTracks(tracks, startIndex.coerceIn(tracks.indices), 0L, shouldPlay = true)
    }

    private fun loadTracks(tracks: List<Track>, startIndex: Int, positionMs: Long, shouldPlay: Boolean) {
        val safeIndex = startIndex.coerceIn(tracks.indices)
        _queue.value = tracks
        _currentTrack.value = tracks[safeIndex]
        val mediaItems = tracks.map(::createMediaItem)
        exoPlayer?.apply {
            setMediaItems(mediaItems, safeIndex, positionMs)
            prepare()
            if (shouldPlay) play() else pause()
        }
        updateProgress()
    }

    private fun createMediaItem(track: Track): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.albumArtUri)
            .build()
        return MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.uri)
            .setMediaMetadata(metadata)
            .build()
    }

    fun skipToNext() {
        exoPlayer?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        exoPlayer?.seekToPreviousMediaItem()
    }

    fun toggleShuffle() {
        exoPlayer?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun cycleRepeatMode() {
        exoPlayer?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    private fun updateProgress() {
        val player = exoPlayer ?: return
        _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
        _duration.value = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L
    }

    private fun observeProgress() {
        if (progressJob?.isActive == true) return
        progressJob = progressScope.launch {
            while (isActive && exoPlayer?.isPlaying == true) {
                updateProgress()
                delay(400L)
            }
            updateProgress()
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        updateProgress()
    }

    fun updateSettings(newSettings: AudiophileSettings) {
        val tracks = _queue.value
        val index = exoPlayer?.currentMediaItemIndex ?: 0
        val positionMs = exoPlayer?.currentPosition ?: 0L
        val wasPlaying = exoPlayer?.isPlaying == true
        val shuffleWasEnabled = exoPlayer?.shuffleModeEnabled == true
        val previousRepeatMode = exoPlayer?.repeatMode ?: Player.REPEAT_MODE_OFF
        _settings.value = newSettings
        buildPlayer()
        exoPlayer?.apply {
            shuffleModeEnabled = shuffleWasEnabled
            repeatMode = previousRepeatMode
        }
        if (tracks.isNotEmpty()) {
            loadTracks(tracks, index.coerceIn(tracks.indices), positionMs, shouldPlay = wasPlaying)
        }
    }

    fun isBitPerfectOutputConfirmed(): Boolean = false

    fun isSystemAudioCaptureBlocked(): Boolean = _settings.value.bypassSystemFx

    fun isHighResolutionFloatOutputEnabled(): Boolean = _settings.value.highResolutionOutput

    fun release() {
        progressJob?.cancel()
        progressScope.cancel()
        mediaSession?.release()
        exoPlayer?.release()
        mediaSession = null
        exoPlayer = null
    }
}
