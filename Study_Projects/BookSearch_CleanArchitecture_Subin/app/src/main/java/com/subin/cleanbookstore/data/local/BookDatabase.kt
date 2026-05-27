package com.subin.cleanbookstore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.subin.cleanbookstore.data.local.dao.BookmarkDao
import com.subin.cleanbookstore.data.local.dao.SearchHistoryDao
import com.subin.cleanbookstore.data.local.entity.BookmarkEntity
import com.subin.cleanbookstore.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        BookmarkEntity::class,
        SearchHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    abstract fun searchHistoryDao(): SearchHistoryDao
}