package com.aether.player.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aether.player.audio.AudioPlayerEngine
import com.aether.player.data.AudioScanner
import com.aether.player.model.AudioFolder
import com.aether.player.model.AudiophileSettings
import com.aether.player.model.Track
import com.aether.player.service.PlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = AudioScanner(application)
    val playerEngine = AudioPlayerEngine(application)

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    private val _folders = MutableStateFlow<List<AudioFolder>>(emptyList())
    val folders: StateFlow<List<AudioFolder>> = _folders.asStateFlow()

    private val _selectedFolder = MutableStateFlow<AudioFolder?>(null)
    val selectedFolder: StateFlow<AudioFolder?> = _selectedFolder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Expose media session for background notification service
        PlaybackService.currentMediaSession = playerEngine.mediaSession
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = scanner.scanTracks()
            _allTracks.value = list
            _folders.value = scanner.groupFolders(list)
            _isLoading.value = false
        }
    }

    fun selectFolder(folder: AudioFolder?) {
        _selectedFolder.value = folder
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playTrack(track: Track) {
        playerEngine.playTrack(track)
        startPlaybackService()
    }

    fun togglePlayPause() {
        playerEngine.togglePlayPause()
    }

    fun updateSettings(settings: AudiophileSettings) {
        playerEngine.updateSettings(settings)
    }

    private fun startPlaybackService() {
        try {
            val intent = Intent(getApplication(), PlaybackService::class.java)
            getApplication<Application>().startService(intent)
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        playerEngine.release()
    }
}
