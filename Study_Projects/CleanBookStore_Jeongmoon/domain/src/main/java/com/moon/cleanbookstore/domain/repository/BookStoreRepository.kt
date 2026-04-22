package com.moon.cleanbookstore.domain.repository

import com.moon.cleanbookstore.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookStoreRepository {
    fun searchBooks(keyword: String, startIndex: Int = 0): Flow<List<Book>>
    fun getBookDetail(id: String): Flow<Book>
    fun getBookmarkedBooks(): Flow<List<Book>>
    fun getBookmarkedBook(id: String): Flow<Book?>
    suspend fun addBookToBookmark(book: Book)
    suspend fun removeBookFromBookmark(id: String)
}