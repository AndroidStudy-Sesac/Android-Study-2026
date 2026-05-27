package com.subin.cleanbookstore.di

import com.subin.cleanbookstore.data.repository.BookSearchRepositoryImpl
import com.subin.cleanbookstore.data.repository.BookmarkRepositoryImpl
import com.subin.cleanbookstore.data.repository.SearchHistoryRepositoryImpl
import com.subin.cleanbookstore.domain.repository.BookSearchRepository
import com.subin.cleanbookstore.domain.repository.BookmarkRepository
import com.subin.cleanbookstore.domain.repository.SearchHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookSearchRepository(
        bookSearchRepositoryImpl: BookSearchRepositoryImpl
    ): BookSearchRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        bookmarkRepositoryImpl: BookmarkRepositoryImpl
    ): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(
        searchHistoryRepositoryImpl: SearchHistoryRepositoryImpl
    ): SearchHistoryRepository
}