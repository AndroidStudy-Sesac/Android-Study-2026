package com.moon.cleanbookstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moon.cleanbookstore.data.local.entity.BookMemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookMemoDao {
    @Query("SELECT * FROM book_memo WHERE id = :id")
    fun getMemoFlow(id: String): Flow<BookMemoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookMemoEntity: BookMemoEntity)
}