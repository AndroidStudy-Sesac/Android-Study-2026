package com.jeong.cleanbookstore.data.di.api

import com.jeong.cleanbookstore.data.response.BookSearchResultResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApiService {
    @GET("books/v1/volumes")
    suspend fun searchBooksByKeyword(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int,
        @Query("maxResults") maxResults: Int,
    ): Response<BookSearchResultResponse>
}
