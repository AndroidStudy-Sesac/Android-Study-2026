package com.moon.cleanbookstore.feature.home

import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.feature.base.State

sealed interface NewTabState : State {
    data object Uninitialized : NewTabState
    data object Loading : NewTabState
    data class Success(val books: List<Book>) : NewTabState
    data class Error(val message: String) : NewTabState
}