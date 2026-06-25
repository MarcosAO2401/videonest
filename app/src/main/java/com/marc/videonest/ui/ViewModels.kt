package com.marc.videonest.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marc.videonest.data.model.Playlist
import com.marc.videonest.data.model.Video
import com.marc.videonest.data.repository.PlaylistRepository
import com.marc.videonest.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(val playlists: List<Playlist> = emptyList(), val isLoading: Boolean = true, val error: String? = null)

@HiltViewModel
class PlaylistViewModel @Inject constructor(private val repo: PlaylistRepository) : ViewModel() {
    val uiState: StateFlow<PlaylistUiState> = repo.getAllPlaylists()
        .map { PlaylistUiState(playlists = it, isLoading = false) }
        .catch { e -> emit(PlaylistUiState(isLoading = false, error = e.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaylistUiState())

    fun createPlaylist(name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try { repo.createPlaylist(name); onResult(true) } catch (e: Exception) { onResult(false) }
        }
    }
    fun deletePlaylist(playlist: Playlist) { viewModelScope.launch { repo.deletePlaylist(playlist) } }
}

data class VideoUiState(val videos: List<Video> = emptyList(), val isLoading: Boolean = true, val error: String? = null)

@HiltViewModel
class VideoViewModel @Inject constructor(private val repo: VideoRepository) : ViewModel() {
    private val _playlistId = MutableStateFlow<Long?>(null)
    private val _search = MutableStateFlow("")

    val uiState: StateFlow<VideoUiState> = _playlistId
        .flatMapLatest { id -> if (id == null) repo.getAllVideos() else repo.getVideosByPlaylist(id) }
        .combine(_search) { videos, q -> if (q.isBlank()) videos else videos.filter { it.title.contains(q, true) || it.note.contains(q, true) } }
        .map { VideoUiState(videos = it, isLoading = false) }
        .catch { e -> emit(VideoUiState(isLoading = false, error = e.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoUiState())

    fun setPlaylist(id: Long?) { _playlistId.value = id }
    fun setSearch(q: String) { _search.value = q }
    fun saveVideo(url: String, playlistId: Long, note: String = "", onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val r = repo.saveVideoFromUrl(url, playlistId, note)
            if (r.isSuccess) onResult(true, "Video guardado") else onResult(false, r.exceptionOrNull()?.message ?: "Error")
        }
    }
    fun deleteVideo(video: Video) { viewModelScope.launch { repo.deleteVideo(video) } }
    fun updateNote(video: Video, note: String) { viewModelScope.launch { repo.updateNote(video, note) } }
}
