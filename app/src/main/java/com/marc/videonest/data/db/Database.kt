package com.marc.videonest.data.db
import androidx.room.*
import com.marc.videonest.data.model.Platform
import com.marc.videonest.data.model.Playlist
import com.marc.videonest.data.model.Video

class Converters {
    @TypeConverter fun fromPlatform(p: Platform): String = p.name
    @TypeConverter fun toPlatform(v: String): Platform = try { Platform.valueOf(v) } catch(e: Exception) { Platform.UNKNOWN }
}

@Database(entities = [Playlist::class, Video::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class VideoNestDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun videoDao(): VideoDao
    companion object { const val DATABASE_NAME = "videonest.db" }
}
