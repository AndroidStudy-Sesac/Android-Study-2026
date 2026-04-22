package com.jeong.cleanbookstore.screen.main.search

import com.jeong.cleanbookstore.model.book.BookModel
import com.jeong.cleanbookstore.screen.base.State

sealed class SearchTabState : State {
    data object Uninitialized : SearchTabState()

    data object Loading : SearchTabState()

    data class Success(
        val books: List<BookModel>,
        val searchKeyword: String = "",
    ) : SearchTabState()

    data class Error(
        val message: String,
        val searchKeyword: String = "",
    ) : SearchTabState()
}
