package com.aether.player.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.aether.player.model.AudiophileSettings
import com.aether.player.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
class AudioPlayerEngine(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    
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
        exoPlayer?.release()

        // Bit-Perfect AudioAttributes: ContentType MUSIC + USAGE_MEDIA
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_NONE) // Audio purist: prevent background capture/mixing
            .build()

        // Configure custom audio sink with direct 32-bit float output
        val audioSink: AudioSink = DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(true) // 32-bit float output preserves maximum dynamic range without 16-bit truncation
            .setEnableAudioTrackPlaybackParams(false) // Disable system pitch/speed stretching to prevent digital resampling
            .build()

        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                return audioSink
            }
        }.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

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
    }

    fun playTrack(track: Track) {
        _currentTrack.value = track
        val mediaItem = MediaItem.fromUri(track.uri)
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
        exoPlayer?.release()
        exoPlayer = null
    }
}
