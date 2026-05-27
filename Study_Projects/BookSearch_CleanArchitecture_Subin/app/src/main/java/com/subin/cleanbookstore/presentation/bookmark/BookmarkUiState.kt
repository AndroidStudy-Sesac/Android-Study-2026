package com.subin.cleanbookstore.presentation.bookmark

import com.subin.cleanbookstore.domain.model.Book

sealed interface BookmarkUiState {
    data object Loading : BookmarkUiState
    data class Success(val bookmarks: List<Book>) : BookmarkUiState
    data class Error(val message: String) : BookmarkUiState
}