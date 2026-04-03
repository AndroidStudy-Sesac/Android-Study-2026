package com.jeong.cleanbookstore.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jeong.cleanbookstore.data.db.dao.BookmarkDao
import com.jeong.cleanbookstore.data.entity.BookmarkEntity

@Database(
    entities = [BookmarkEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DB_NAME: String = "clean_bookstore.db"
    }
}
