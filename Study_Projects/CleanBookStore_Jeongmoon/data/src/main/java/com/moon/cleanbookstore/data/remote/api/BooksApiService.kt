package com.moon.cleanbookstore.data.remote.api

import com.moon.cleanbookstore.data.remote.response.BookItemDto
import com.moon.cleanbookstore.data.remote.response.BookSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BooksApiService {

    /**
     * 구글 도서 검색 API
     */
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int = 0,
        @Query("maxResults") maxResults: Int = 20
    ): BookSearchResponse

    /**
     * 특정 도서 상세 정보 API
     */
    @GET("volumes/{id}")
    suspend fun getBookInfo(
        @Path("id") id: String
    ): BookItemDto
}