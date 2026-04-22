package com.jeong.cleanbookstore.data.di

import android.content.Context
import androidx.room.Room
import com.jeong.cleanbookstore.data.db.BookDatabase
import com.jeong.cleanbookstore.data.db.dao.BookmarkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideBookDatabase(
        @ApplicationContext context: Context,
    ): BookDatabase =
        Room
            .databaseBuilder(
                context,
                BookDatabase::class.java,
                BookDatabase.DB_NAME,
            ).build()

    @Provides
    @Singleton
    fun provideBookmarkDao(database: BookDatabase): BookmarkDao = database.bookmarkDao()
}
