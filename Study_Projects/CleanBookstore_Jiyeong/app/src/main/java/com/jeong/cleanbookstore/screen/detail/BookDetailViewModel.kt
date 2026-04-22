package com.jeong.cleanbookstore.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.jeong.cleanbookstore.data.repository.BookSearchRepository
import com.jeong.cleanbookstore.screen.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel
    @Inject
    constructor(
        private val bookSearchRepository: BookSearchRepository,
        private val savedStateHandle: SavedStateHandle,
    ) : BaseViewModel<BookDetailState, BookDetailEvent>() {
        override fun getInitialState(): BookDetailState = BookDetailState.Uninitialized

        private val bookId: String? by lazy {
            savedStateHandle.get<String>("bookId")
        }

        override fun fetchData(): Job =
            viewModelScope.launch {
                val targetBookId = bookId
                if (targetBookId.isNullOrBlank()) {
                    setState(
                        BookDetailState.Error("도서 식별자가 없습니다."),
                    )
                    return@launch
                }

                try {
                    setState(BookDetailState.Loading)

                    val detail =
                        bookSearchRepository.getBookDetail(
                            volumeId = targetBookId,
                        )

                    setState(
                        BookDetailState.Success(
                            book = detail,
                        ),
                    )
                } catch (e: Exception) {
                    setState(
                        BookDetailState.Error(
                            message = e.message ?: "도서 상세 정보를 불러오지 못했습니다.",
                        ),
                    )
                }
            }
    }
