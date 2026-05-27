package com.subin.cleanbookstore.domain.usecase

import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarkListUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    operator fun invoke(): Flow<List<Book>> {
        return repository.getAllBookmarks()
    }
}