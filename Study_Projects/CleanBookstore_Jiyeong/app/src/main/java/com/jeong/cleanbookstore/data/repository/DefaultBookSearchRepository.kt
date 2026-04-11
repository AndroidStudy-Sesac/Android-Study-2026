package com.jeong.cleanbookstore.data.repository

import com.jeong.cleanbookstore.data.di.api.BooksApiService
import com.jeong.cleanbookstore.data.mapper.toModel
import com.jeong.cleanbookstore.data.mapper.toModelList
import com.jeong.cleanbookstore.model.book.BookModel
import javax.inject.Inject

class DefaultBookSearchRepository
    @Inject
    constructor(
        private val booksApiService: BooksApiService,
    ) : BookSearchRepository {
        private val bookCache = mutableMapOf<String, BookModel>()

        override suspend fun searchBooksByKeyword(
            query: String,
            startIndex: Int,
            maxResults: Int,
        ): List<BookModel> {
            val response = booksApiService.searchBooksByKeyword(query, startIndex, maxResults)
            if (!response.isSuccessful) {
                throw Exception(getErrorMessage(response.code()))
            }
            val body = response.body() ?: throw Exception("검색 결과를 불러오지 못했습니다.")
            val books = body.toModelList().map { book ->
                // 캐시에 더 풍부한 정보(출판사 등)가 있다면 그것을 사용
                val cachedBook = bookCache[book.id]
                if (cachedBook != null) {
                    book.copy(
                        publisher = cachedBook.publisher ?: book.publisher,
                        pageCount = cachedBook.pageCount ?: book.pageCount,
                        categories = cachedBook.categories.ifEmpty { book.categories },
                        description = cachedBook.description?.takeIf { it.isNotBlank() } ?: book.description
                    )
                } else {
                    book
                }
            }
            return books
        }

        override suspend fun getBookDetail(volumeId: String): BookModel {
            val response = booksApiService.getBookDetail(volumeId)
            if (!response.isSuccessful) {
                throw Exception(getErrorMessage(response.code()))
            }
            val body = response.body() ?: throw Exception("도서 상세 정보를 불러오지 못했습니다.")
            val book = body.toModel()
            // 상세 정보를 캐시에 저장하여 리스트에서도 사용할 수 있게 함
            bookCache[volumeId] = book
            return book
        }

        private fun getErrorMessage(code: Int): String =
            when (code) {
                403 -> "API 요청이 제한되었습니다."
                429 -> "API 요청 제한이 초과되었습니다."
                in 500..599 -> "서버 내부 오류가 발생했습니다."
                else -> "도서 검색 중 오류가 발생했습니다. (code=$code)"
            }
    }
