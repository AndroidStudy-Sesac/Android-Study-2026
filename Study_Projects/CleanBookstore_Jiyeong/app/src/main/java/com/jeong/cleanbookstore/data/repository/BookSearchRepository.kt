package com.jeong.cleanbookstore.data.repository

import com.jeong.cleanbookstore.model.book.BookModel

interface BookSearchRepository {
    suspend fun searchBooksByKeyword(
        query: String,
        startIndex: Int = 0,
        maxResults: Int = 20,
    ): List<BookModel>

    suspend fun getBookDetail(volumeId: String): BookModel
}
