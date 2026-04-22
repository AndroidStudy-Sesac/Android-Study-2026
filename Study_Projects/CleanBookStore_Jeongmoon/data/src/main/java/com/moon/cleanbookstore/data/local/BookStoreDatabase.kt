package com.moon.cleanbookstore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moon.cleanbookstore.data.local.dao.BookInWishListDao
import com.moon.cleanbookstore.data.local.dao.BookMemoDao
import com.moon.cleanbookstore.data.local.dao.SearchHistoryDao
import com.moon.cleanbookstore.data.local.entity.BookEntity
import com.moon.cleanbookstore.data.local.entity.BookMemoEntity
import com.moon.cleanbookstore.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        BookEntity::class,
        SearchHistoryEntity::class,
        BookMemoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BookStoreDatabase : RoomDatabase() {
    abstract fun bookInWishListDao(): BookInWishListDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun bookMemoDao(): BookMemoDao
}