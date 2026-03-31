package com.moon.cleanbookstore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moon.cleanbookstore.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookInWishListDao {
    @Query("SELECT * FROM book_wish_list")
    fun getAllBookmarksFlow(): Flow<List<BookEntity>>

    @Query("SELECT * FROM book_wish_list WHERE id = :id")
    fun getBookmarkFlow(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM book_wish_list WHERE id = :id")
    suspend fun get(id: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookEntity: BookEntity)

    @Query("DELETE FROM book_wish_list WHERE id = :id")
    suspend fun delete(id: String)
}