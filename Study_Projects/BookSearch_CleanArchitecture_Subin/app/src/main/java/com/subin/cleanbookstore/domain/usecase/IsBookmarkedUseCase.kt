package com.subin.cleanbookstore.domain.usecase

import com.subin.cleanbookstore.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsBookmarkedUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    operator fun invoke(bookId: String): Flow<Boolean> {
        return repository.isBookmarked(bookId)
    }
}