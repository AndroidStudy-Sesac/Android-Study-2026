package com.jeong.cleanbookstore.screen.main.bookmark

import androidx.lifecycle.viewModelScope
import com.jeong.cleanbookstore.data.repository.BookmarkRepository
import com.jeong.cleanbookstore.screen.base.BaseViewModel
import com.jeong.cleanbookstore.screen.base.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel
    @Inject
    constructor(
        private val bookmarkRepository: BookmarkRepository,
    ) : BaseViewModel<BookmarkState, Event>() {
        override fun getInitialState(): BookmarkState = BookmarkState.Uninitialized

        override fun fetchData(): Job =
            viewModelScope.launch {
                try {
                    setState(
                        BookmarkState.Loading,
                    )
                    bookmarkRepository.getBookmarks().collect { books ->
                        if (books.isEmpty()) {
                            setState(BookmarkState.Empty)
                        } else {
                            setState(
                                BookmarkState.Success(
                                    books = books,
                                ),
                            )
                        }
                    }
                } catch (e: Exception) {
                    setState(
                        BookmarkState.Error(
                            message = e.message ?: "북마크 목록을 불러오지 못했습니다.",
                        ),
                    )
                }
            }

        fun removeBookmark(id: String) =
            viewModelScope.launch {
                try {
                    bookmarkRepository.removeBookmark(id)
                } catch (e: Exception) {
                    setState(
                        BookmarkState.Error(
                            message = e.message ?: "북마크를 삭제하지 못했습니다.",
                        ),
                    )
                }
            }
    }
