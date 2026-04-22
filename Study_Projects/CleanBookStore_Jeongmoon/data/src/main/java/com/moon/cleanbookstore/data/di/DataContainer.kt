package com.moon.cleanbookstore.data.di

import android.content.Context
import androidx.room.Room
import com.moon.cleanbookstore.data.BuildConfig
import com.moon.cleanbookstore.data.local.BookStoreDatabase
import com.moon.cleanbookstore.data.remote.api.ApiKeyInterceptor
import com.moon.cleanbookstore.data.remote.api.BooksApiService
import com.moon.cleanbookstore.data.repository.BookMemoRepositoryImpl
import com.moon.cleanbookstore.data.repository.BookSearchRepositoryImpl
import com.moon.cleanbookstore.data.repository.BookStoreRepositoryImpl
import com.moon.cleanbookstore.domain.repository.BookMemoRepository
import com.moon.cleanbookstore.domain.repository.BookSearchRepository
import com.moon.cleanbookstore.domain.repository.BookStoreRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DataContainer(private val context: Context) {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(BuildConfig.GOOGLE_BOOKS_API_KEY))
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/books/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: BooksApiService = retrofit.create(BooksApiService::class.java)

    private val database: BookStoreDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            BookStoreDatabase::class.java,
            "book_store_database"
        ).build()
    }

    val bookStoreRepository: BookStoreRepository by lazy {
        BookStoreRepositoryImpl(
            apiService = apiService,
            bookInWishListDao = database.bookInWishListDao()
        )
    }

    val bookSearchRepository: BookSearchRepository by lazy {
        BookSearchRepositoryImpl(
            searchHistoryDao = database.searchHistoryDao()
        )
    }

    val bookMemoRepository: BookMemoRepository by lazy {
        BookMemoRepositoryImpl(
            bookMemoDao = database.bookMemoDao()
        )
    }
}