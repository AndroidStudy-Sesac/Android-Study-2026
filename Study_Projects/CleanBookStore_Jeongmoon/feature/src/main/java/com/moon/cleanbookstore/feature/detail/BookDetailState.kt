package com.moon.cleanbookstore.feature.detail

import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.domain.model.BookMemo
import com.moon.cleanbookstore.feature.base.State

sealed interface BookDetailState : State {
    data object Uninitialized : BookDetailState
    data object Loading : BookDetailState
    data class Success(
        val book: Book,
        val memo: BookMemo? = null
    ) : BookDetailState
    data class Error(val message: String) : BookDetailState
}