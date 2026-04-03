package com.jeong.cleanbookstore.data.repository

import com.jeong.cleanbookstore.model.book.BookModel
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getBookmarks(): Flow<List<BookModel>>

    fun getBookmark(id: String): Flow<BookModel?>

    suspend fun addBookmark(book: BookModel)

    suspend fun removeBookmark(id: String)
}
