package com.subin.cleanbookstore.data.di

import android.content.Context
import androidx.room.Room
import com.subin.cleanbookstore.data.local.BookDatabase
import com.subin.cleanbookstore.data.local.dao.BookmarkDao
import com.subin.cleanbookstore.data.local.dao.SearchHistoryDao
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
        @ApplicationContext context: Context
    ): BookDatabase {
        return Room.databaseBuilder(
            context,
            BookDatabase::class.java,
            "book_store.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: BookDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: BookDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }
}