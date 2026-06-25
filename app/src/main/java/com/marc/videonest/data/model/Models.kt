package com.marc.videonest.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Platform(val displayName: String) {
    YOUTUBE("YouTube"), TIKTOK("TikTok"), INSTAGRAM("Instagram"),
    TWITTER("X / Twitter"), FACEBOOK("Facebook"), UNKNOWN("Otro")
}

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val platform: Platform,
    val playlistId: Long,
    val note: String = "",
    val isAlive: Boolean = true,
    val savedAt: Long = System.currentTimeMillis(),
    val lastCheckedAt: Long = System.currentTimeMillis()
)
