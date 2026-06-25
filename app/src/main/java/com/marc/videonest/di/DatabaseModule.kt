package com.marc.videonest.di
import android.content.Context
import androidx.room.Room
import com.marc.videonest.data.db.VideoNestDatabase
import com.marc.videonest.data.db.PlaylistDao
import com.marc.videonest.data.db.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VideoNestDatabase =
        Room.databaseBuilder(context, VideoNestDatabase::class.java, VideoNestDatabase.DATABASE_NAME).build()
    @Provides fun providePlaylistDao(db: VideoNestDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideVideoDao(db: VideoNestDatabase): VideoDao = db.videoDao()
}
