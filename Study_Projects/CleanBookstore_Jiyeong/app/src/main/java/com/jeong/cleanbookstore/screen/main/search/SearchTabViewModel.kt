package com.jeong.cleanbookstore.screen.main.search

import androidx.lifecycle.viewModelScope
import com.jeong.cleanbookstore.data.repository.BookSearchRepository
import com.jeong.cleanbookstore.data.repository.BookmarkRepository
import com.jeong.cleanbookstore.model.book.BookModel
import com.jeong.cleanbookstore.screen.base.BaseViewModel
import com.jeong.cleanbookstore.screen.base.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchTabViewModel
    @Inject
    constructor(
        private val bookSearchRepository: BookSearchRepository,
        private val bookmarkRepository: BookmarkRepository,
    ) : BaseViewModel<SearchTabState, Event>() {
        override fun getInitialState(): SearchTabState = SearchTabState.Uninitialized

        private var lastQuery: String = ""
        private var rawSearchBooks: List<BookModel> = emptyList()

        init {
            observeBookmarks()
        }

        override fun fetchData(): Job =
            viewModelScope.launch {
                setState(
                    SearchTabState.Success(
                        books = emptyList(),
                        searchKeyword = "",
                    ),
                )
            }

        fun searchByKeyword(keyword: String) {
            val trimmedKeyword = keyword.trim()

            if (trimmedKeyword.isBlank()) {
                return
            }

            viewModelScope.launch {
                try {
                    setState(SearchTabState.Loading)

                    lastQuery = trimmedKeyword
                    rawSearchBooks =
                        bookSearchRepository.searchBooksByKeyword(
                            query = trimmedKeyword,
                        )
                    emitMergedSearchState()
                } catch (e: Exception) {
                    setState(
                        SearchTabState.Error(
                            message = e.message ?: "Unknown Error",
                            searchKeyword = trimmedKeyword,
                        ),
                    )
                }
            }
        }

        fun toggleBookmark(book: BookModel) {
            viewModelScope.launch {
                try {
                    val isBookmarked = book.isLiked == true

                    if (isBookmarked) {
                        bookmarkRepository.removeBookmark(book.id)
                    } else {
                        bookmarkRepository.addBookmark(book)
                    }
                } catch (e: Exception) {
                    setState(
                        SearchTabState.Error(
                            message = e.message ?: "북마크 처리 중 오류가 발생했습니다.",
                            searchKeyword = lastQuery,
                        ),
                    )
                }
            }
        }

        private fun observeBookmarks() {
            viewModelScope.launch {
                bookmarkRepository.getBookmarks().collectLatest {
                    emitMergedSearchState()
                }
            }
        }

        private suspend fun emitMergedSearchState() {
            val bookmarkedIds =
                bookmarkRepository
                    .getBookmarkIds()

            val mergedBooks =
                rawSearchBooks.map { book ->
                    book.copy(
                        isLiked = bookmarkedIds.contains(book.id),
                    )
                }

            setState(
                SearchTabState.Success(
                    books = mergedBooks,
                    searchKeyword = lastQuery,
                ),
            )
        }
    }
