package com.jeong.cleanbookstore.data.di.api

import com.jeong.cleanbookstore.data.repository.BookSearchRepository
import com.jeong.cleanbookstore.data.repository.BookmarkRepository
import com.jeong.cleanbookstore.data.repository.DefaultBookSearchRepository
import com.jeong.cleanbookstore.data.repository.DefaultBookmarkRepository
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
    abstract fun bindBookSearchRepository(defaultBookSearchRepository: DefaultBookSearchRepository): BookSearchRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(defaultBookmarkRepository: DefaultBookmarkRepository): BookmarkRepository
}
