package com.byben.sonara.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.byben.sonara.data.model.Song
import com.byben.sonara.data.repository.MusicRepository
import com.byben.sonara.player.SonaraPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF,
    ONE,
    ALL
}

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val songs: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isRefreshing: Boolean = false
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        loadSongs()
        connectToService()
        startPositionUpdater()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            val songs = repository.getAllSongs()
            _state.value = _state.value.copy(songs = songs)
        }
    }

    fun refreshSongs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                val songs = repository.getAllSongs()
                _state.value = _state.value.copy(songs = songs, isRefreshing = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

    fun playPrevious() {
        controller?.seekToPreviousMediaItem()
    }

    fun playNext() {
        controller?.seekToNextMediaItem()
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun toggleShuffle() {
        controller?.let { ctrl ->
            val enabled = !(ctrl.shuffleModeEnabled ?: false)
            ctrl.setShuffleModeEnabled(enabled)
            _state.value = _state.value.copy(isShuffleEnabled = enabled)
        }
    }

    fun toggleRepeat() {
        val nextMode = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        controller?.setRepeatMode(
            when (nextMode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }
        )
        _state.value = _state.value.copy(repeatMode = nextMode)
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), SonaraPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            val songs = _state.value.songs
            val index = controller?.currentMediaItemIndex ?: 0
            if (songs.isNotEmpty() && index < songs.size) {
                _state.value = _state.value.copy(
                    currentSong = songs[index],
                    currentIndex = index,
                    duration = controller?.duration?.takeIf { it > 0 } ?: 0L
                )
            }
        }
    }

    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (true) {
                controller?.let { ctrl ->
                    val pos = ctrl.currentPosition.coerceAtLeast(0)
                    val dur = ctrl.duration.takeIf { it > 0 } ?: 0L
                    _state.value = _state.value.copy(
                        currentPosition = pos,
                        duration = dur
                    )
                }
                delay(500)
            }
        }
    }

    fun playSong(song: Song, index: Int) {
        val ctrl = controller ?: return
        val mediaItems = _state.value.songs.map { s ->
            MediaItem.Builder()
                .setUri(Uri.parse(s.uri))
                .setMediaId(s.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(s.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
        }
        ctrl.setMediaItems(mediaItems, index, 0L)
        ctrl.prepare()
        ctrl.play()
        _state.value = _state.value.copy(
            currentSong = song,
            currentIndex = index
        )
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (_state.value.currentSong == null && _state.value.songs.isNotEmpty()) {
            playSong(_state.value.songs[0], 0)
            return
        }
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture ?: return)
        super.onCleared()
    }
}
