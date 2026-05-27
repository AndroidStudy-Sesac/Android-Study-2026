package com.subin.cleanbookstore.domain.usecase

import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(book: Book) {
        val isBookmarked = repository.isBookmarked(book.id).first()

        if (isBookmarked) {
            repository.deleteBookmark(book.id)
        } else {
            repository.saveBookmark(book)
        }
    }
}