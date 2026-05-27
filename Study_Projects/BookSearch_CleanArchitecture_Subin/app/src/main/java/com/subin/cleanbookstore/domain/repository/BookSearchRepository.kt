package com.subin.cleanbookstore.domain.repository

import androidx.paging.PagingData
import com.subin.cleanbookstore.core.DataResult
import com.subin.cleanbookstore.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookSearchRepository {
    fun searchBooks(query: String): Flow<PagingData<Book>>
    suspend fun getBookDetails(bookId: String): DataResult<Book>
}