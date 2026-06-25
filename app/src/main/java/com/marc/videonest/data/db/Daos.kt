package com.marc.videonest.data.db
import androidx.room.*
import com.marc.videonest.data.model.Playlist
import com.marc.videonest.data.model.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long
    @Update
    suspend fun updatePlaylist(playlist: Playlist)
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
    @Query("SELECT COUNT(*) FROM videos WHERE playlistId = :playlistId")
    fun getVideoCountForPlaylist(playlistId: Long): Flow<Int>
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE playlistId = :playlistId ORDER BY savedAt DESC")
    fun getVideosByPlaylist(playlistId: Long): Flow<List<Video>>
    @Query("SELECT * FROM videos ORDER BY savedAt DESC")
    fun getAllVideos(): Flow<List<Video>>
    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchVideos(query: String): Flow<List<Video>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video): Long
    @Update
    suspend fun updateVideo(video: Video)
    @Delete
    suspend fun deleteVideo(video: Video)
    @Query("UPDATE videos SET isAlive = :isAlive, lastCheckedAt = :checkedAt WHERE id = :videoId")
    suspend fun updateVideoStatus(videoId: Long, isAlive: Boolean, checkedAt: Long)
    @Query("SELECT * FROM videos WHERE lastCheckedAt < :threshold")
    suspend fun getVideosToCheck(threshold: Long): List<Video>
}
