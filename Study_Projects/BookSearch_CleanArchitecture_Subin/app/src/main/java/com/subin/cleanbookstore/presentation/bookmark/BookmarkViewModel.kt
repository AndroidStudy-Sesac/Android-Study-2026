package com.subin.cleanbookstore.presentation.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.domain.usecase.GetBookmarkListUseCase
import com.subin.cleanbookstore.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val getBookmarkListUseCase: GetBookmarkListUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    val uiState: StateFlow<BookmarkUiState> = getBookmarkListUseCase()
        .map { books ->
            BookmarkUiState.Success(books) as BookmarkUiState
        }
        .catch { e ->
            emit(BookmarkUiState.Error(e.message ?: "알 수 없는 오류"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BookmarkUiState.Loading
        )

    fun onBookmarkClick(book: Book) {
        viewModelScope.launch {
            toggleBookmarkUseCase(book)
        }
    }
}