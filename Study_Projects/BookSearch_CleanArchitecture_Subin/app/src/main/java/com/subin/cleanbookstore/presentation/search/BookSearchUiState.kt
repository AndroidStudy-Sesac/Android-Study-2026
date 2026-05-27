package com.subin.cleanbookstore.presentation.search

import com.subin.cleanbookstore.domain.model.Book

sealed interface BookSearchUiState {
    data object Loading : BookSearchUiState
    data class Success(val books: List<Book>) : BookSearchUiState
    data class Error(val message: String) : BookSearchUiState
    data object Empty : BookSearchUiState
}

