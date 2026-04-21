package com.moon.cleanbookstore.feature.search

import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.feature.base.State

sealed interface SearchTabState : State {
    data object Uninitialized : SearchTabState
    data object Loading : SearchTabState

    data class History(val keywords: List<String>) : SearchTabState

    data class Success(
        val books: List<Book>,
        val currentKeyword: String
    ) : SearchTabState

    data class Error(val message: String) : SearchTabState
}