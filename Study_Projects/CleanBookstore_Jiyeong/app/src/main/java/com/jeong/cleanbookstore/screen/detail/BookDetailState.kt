package com.jeong.cleanbookstore.screen.detail

import com.jeong.cleanbookstore.model.book.BookModel
import com.jeong.cleanbookstore.screen.base.State

sealed class BookDetailState : State {
    data object Uninitialized : BookDetailState()

    data object Loading : BookDetailState()

    data class Success(
        val book: BookModel,
    ) : BookDetailState()

    data class Error(
        val message: String,
    ) : BookDetailState()
}
