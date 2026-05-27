package com.subin.cleanbookstore.domain.repository

import com.subin.cleanbookstore.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getAllBookmarks(): Flow<List<Book>>
    suspend fun saveBookmark(book: Book)
    suspend fun deleteBookmark(bookId: String)
    fun isBookmarked(bookId: String): Flow<Boolean>

    suspend fun updateMemo(bookId: String, memo: String)
}