package com.subin.cleanbookstore.domain.usecase

import com.subin.cleanbookstore.core.DataResult
import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.domain.repository.BookSearchRepository
import com.subin.cleanbookstore.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetBookDetailUseCase @Inject constructor(
    private val searchRepository: BookSearchRepository,
    private val bookmarkRepository: BookmarkRepository
) {
    operator fun invoke(bookId: String): Flow<DataResult<Book>> = flow {
        val apiResult = searchRepository.getBookDetails(bookId)

        if (apiResult is DataResult.Success) {
            val remoteBook = apiResult.data

            bookmarkRepository.getAllBookmarks().collect { bookmarks ->
                val bookmarkedItem = bookmarks.find { it.id == bookId }

                val finalBook = remoteBook.copy(
                    isFavorite = bookmarkedItem != null,
                    memo = bookmarkedItem?.memo ?: ""
                )
                emit(DataResult.Success(finalBook))
            }
        } else if (apiResult is DataResult.Error) {
            emit(DataResult.Error(apiResult.exception))
        }
    }
}