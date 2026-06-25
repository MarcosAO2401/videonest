package com.marc.videonest.data.repository
import com.marc.videonest.data.db.PlaylistDao
import com.marc.videonest.data.db.VideoDao
import com.marc.videonest.data.model.Platform
import com.marc.videonest.data.model.Playlist
import com.marc.videonest.data.model.Video
import com.marc.videonest.util.extractor.UrlExtractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(private val playlistDao: PlaylistDao) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists().distinctUntilChanged()
    fun getVideoCount(playlistId: Long): Flow<Int> = playlistDao.getVideoCountForPlaylist(playlistId).distinctUntilChanged()
    suspend fun createPlaylist(name: String): Long {
        val trimmed = name.trim().take(100)
        if (trimmed.isEmpty()) throw IllegalArgumentException("Nombre vacio")
        return playlistDao.insertPlaylist(Playlist(name = trimmed))
    }
    suspend fun renamePlaylist(playlist: Playlist, newName: String) {
        val trimmed = newName.trim().take(100)
        if (trimmed.isEmpty()) throw IllegalArgumentException("Nombre vacio")
        playlistDao.updatePlaylist(playlist.copy(name = trimmed, updatedAt = System.currentTimeMillis()))
    }
    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.deletePlaylist(playlist)
}

@Singleton
class VideoRepository @Inject constructor(private val videoDao: VideoDao) {
    fun getVideosByPlaylist(playlistId: Long): Flow<List<Video>> = videoDao.getVideosByPlaylist(playlistId).distinctUntilChanged()
    fun getAllVideos(): Flow<List<Video>> = videoDao.getAllVideos().distinctUntilChanged()
    fun searchVideos(query: String): Flow<List<Video>> = videoDao.searchVideos(query.trim()).distinctUntilChanged()

    suspend fun saveVideoFromUrl(url: String, playlistId: Long, note: String = ""): Result<Long> {
        return try {
            val metadata = UrlExtractor.extract(url) ?: return Result.failure(IllegalArgumentException("URL no valida"))
            val title = when {
                metadata.videoId != null -> "${metadata.platform.displayName} · ${metadata.videoId}"
                else -> metadata.platform.displayName
            }
            val video = Video(
                url = metadata.sanitizedUrl, title = title,
                thumbnailUrl = metadata.thumbnailUrl, platform = metadata.platform,
                playlistId = playlistId, note = note.trim().take(500)
            )
            Result.success(videoDao.insertVideo(video))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateNote(video: Video, note: String) = videoDao.updateVideo(video.copy(note = note.trim().take(500)))
    suspend fun deleteVideo(video: Video) = videoDao.deleteVideo(video)
    suspend fun updateVideoStatus(videoId: Long, isAlive: Boolean) = videoDao.updateVideoStatus(videoId, isAlive, System.currentTimeMillis())
    suspend fun getVideosToCheck(): List<Video> {
        val threshold = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        return videoDao.getVideosToCheck(threshold)
    }
}
