package com.jeong.cleanbookstore.data.repository

import com.jeong.cleanbookstore.data.di.api.BooksApiService
import com.jeong.cleanbookstore.data.mapper.toModelList
import com.jeong.cleanbookstore.model.book.BookModel
import javax.inject.Inject

class DefaultBookSearchRepository
    @Inject
    constructor(
        private val booksApiService: BooksApiService,
    ) : BookSearchRepository {
        override suspend fun searchBooksByKeyword(
            query: String,
            startIndex: Int,
            maxResults: Int,
        ): List<BookModel> {
            val response = booksApiService.searchBooksByKeyword(query, startIndex, maxResults)
            if (!response.isSuccessful) {
                return emptyList()
            }
            val body = response.body()
            return body?.toModelList() ?: emptyList()
        }
    }
