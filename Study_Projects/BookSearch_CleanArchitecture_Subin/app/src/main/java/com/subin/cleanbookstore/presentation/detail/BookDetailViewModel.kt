package com.subin.cleanbookstore.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subin.cleanbookstore.core.DataResult
import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.domain.usecase.GetBookDetailUseCase
import com.subin.cleanbookstore.domain.usecase.ToggleBookmarkUseCase
import com.subin.cleanbookstore.domain.usecase.UpdateMemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val getBookDetailUseCase: GetBookDetailUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadBookDetail(bookId: String) {
        viewModelScope.launch {
            getBookDetailUseCase(bookId).collect { result ->
                when (result) {
                    is DataResult.Success -> {
                        _uiState.value = DetailUiState.Success(result.data)
                    }
                    is DataResult.Error -> {
                        _uiState.value = DetailUiState.Error(result.exception.message ?: "알 수 없는 에러")
                    }
                }
            }
        }
    }
    fun updateMemo(bookId: String, memo: String) {
        viewModelScope.launch {
            updateMemoUseCase(bookId, memo)
        }
    }

    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            toggleBookmarkUseCase(book)
        }
    }
}

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val book: Book) : DetailUiState
    data class Error(val message: String) : DetailUiState
}