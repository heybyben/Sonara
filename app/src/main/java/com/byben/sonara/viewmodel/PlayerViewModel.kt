package com.byben.sonara.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.byben.sonara.data.model.Song
import com.byben.sonara.data.repository.MusicRepository
import com.byben.sonara.player.SonaraPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF, // 0=off, 1=one, 2=all
    val playbackSpeed: Float = 1f,
    val sleepTimerMinutes: Int = 0,
    val sleepTimerActive: Boolean = false,
    val songs: List<Song> = emptyList(),
    val currentIndex: Int = 0
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var sleepTimerJob: Job? = null

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
        loadSongs()
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

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.value = _state.value.copy(shuffleEnabled = shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _state.value = _state.value.copy(repeatMode = repeatMode)
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
        val songs = _state.value.songs
        val mediaItems = songs.map { s ->
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

    fun skipNext() {
        controller?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        val ctrl = controller ?: return
        if ((ctrl.currentPosition) > 3000L) {
            ctrl.seekTo(0)
        } else {
            ctrl.seekToPreviousMediaItem()
        }
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
        _state.value = _state.value.copy(currentPosition = position)
    }

    fun toggleShuffle() {
        val ctrl = controller ?: return
        val newState = !_state.value.shuffleEnabled
        ctrl.shuffleModeEnabled = newState
        _state.value = _state.value.copy(shuffleEnabled = newState)
    }

    fun toggleRepeat() {
        val ctrl = controller ?: return
        val newMode = when (_state.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        ctrl.repeatMode = newMode
        _state.value = _state.value.copy(repeatMode = newMode)
    }

    fun setPlaybackSpeed(speed: Float) {
        val ctrl = controller ?: return
        ctrl.playbackParameters = PlaybackParameters(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _state.value = _state.value.copy(sleepTimerMinutes = 0, sleepTimerActive = false)
            return
        }

        _state.value = _state.value.copy(
            sleepTimerMinutes = minutes,
            sleepTimerActive = true
        )

        sleepTimerJob = viewModelScope.launch {
            delay(minutes * 60_000L)
            controller?.pause()
            _state.value = _state.value.copy(isPlaying = false, sleepTimerActive = false)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _state.value = _state.value.copy(sleepTimerMinutes = 0, sleepTimerActive = false)
    }

    fun toggleFavorite(song: Song) {
        val songs = _state.value.songs.map {
            if (it.id == song.id) it.copy(isFavorite = !it.isFavorite) else it
        }
        val updatedCurrent = if (_state.value.currentSong?.id == song.id) {
            _state.value.currentSong?.copy(isFavorite = !song.isFavorite)
        } else _state.value.currentSong
        _state.value = _state.value.copy(songs = songs, currentSong = updatedCurrent)
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture ?: return)
        super.onCleared()
    }
}
