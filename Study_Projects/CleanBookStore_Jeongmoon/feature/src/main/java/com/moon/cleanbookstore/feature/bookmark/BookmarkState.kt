package com.moon.cleanbookstore.feature.bookmark

import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.feature.base.State

sealed interface BookmarkState : State {
    data object Uninitialized : BookmarkState
    data object Loading : BookmarkState
    data class Success(val books: List<Book>) : BookmarkState
    data class Error(val message: String) : BookmarkState
}