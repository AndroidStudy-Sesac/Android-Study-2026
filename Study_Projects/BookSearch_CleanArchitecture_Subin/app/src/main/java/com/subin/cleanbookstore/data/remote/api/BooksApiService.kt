package com.subin.cleanbookstore.data.remote.api

import com.subin.cleanbookstore.BuildConfig
import com.subin.cleanbookstore.data.remote.dto.BookDto
import com.subin.cleanbookstore.data.remote.dto.BookSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface BooksApiService {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int = 0,
        @Query("maxResults") maxResults: Int = 10,
        @Query("key") apiKey: String = BuildConfig.BOOKS_API_KEY
    ): BookSearchResponse

    @GET("volumes/{volumeId}")
    suspend fun getBookDetails(
        @Path("volumeId") volumeId: String,
        @Query("key") apiKey: String = BuildConfig.BOOKS_API_KEY
    ): BookDto

}