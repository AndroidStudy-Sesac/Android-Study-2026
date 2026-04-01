package com.jeong.cleanbookstore.data.repository

import com.jeong.cleanbookstore.data.di.api.BooksApiService
import com.jeong.cleanbookstore.data.mapper.toDetailModel
import com.jeong.cleanbookstore.data.mapper.toModelList
import com.jeong.cleanbookstore.model.book.BookDetailModel
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
                throw Exception(getErrorMessage(response.code()))
            }
            val body = response.body() ?: throw Exception("검색 결과를 불러오지 못했습니다.")
            return body.toModelList()
        }

        override suspend fun getBookDetail(volumeId: String): BookDetailModel {
            val response = booksApiService.getBookDetail(volumeId)
            if (!response.isSuccessful) {
                throw Exception(getErrorMessage(response.code()))
            }
            val body = response.body() ?: throw Exception("도서 상세 정보를 불러오지 못했습니다.")
            return body.toDetailModel()
        }

        private fun getErrorMessage(code: Int): String =
            when (code) {
                403 -> "API 요청이 제한되었습니다."
                429 -> "API 요청 제한이 초과되었습니다."
                in 500..599 -> "서버 내부 오류가 발생했습니다."
                else -> "도서 검색 중 오류가 발생했습니다. (code=$code)"
            }
    }
