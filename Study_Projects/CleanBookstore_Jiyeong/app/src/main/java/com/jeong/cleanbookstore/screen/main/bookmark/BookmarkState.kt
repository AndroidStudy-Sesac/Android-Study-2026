package com.jeong.cleanbookstore.screen.main.bookmark

import com.jeong.cleanbookstore.model.book.BookModel
import com.jeong.cleanbookstore.screen.base.State

sealed class BookmarkState : State {
    data object Uninitialized : BookmarkState()

    data object Loading : BookmarkState()

    data object Empty : BookmarkState()

    data class Success(
        val books: List<BookModel>,
    ) : BookmarkState()

    data class Error(
        val message: String,
    ) : BookmarkState()
}
