package com.jeong.cleanbookstore.screen.main.search

import androidx.lifecycle.viewModelScope
import com.jeong.cleanbookstore.data.repository.BookSearchRepository
import com.jeong.cleanbookstore.screen.base.BaseViewModel
import com.jeong.cleanbookstore.screen.base.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchTabViewModel
    @Inject
    constructor(
        private val bookSearchRepository: BookSearchRepository,
    ) : BaseViewModel<SearchTabState, Event>() {
        override fun getInitialState(): SearchTabState = SearchTabState.Uninitialized

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
            if (keyword.isBlank()) {
                return
            }

            viewModelScope.launch {
                try {
                    setState(SearchTabState.Loading)

                    val books =
                        bookSearchRepository.searchBooksByKeyword(query = keyword.trim())
                    setState(
                        SearchTabState.Success(
                            books = books,
                            searchKeyword = keyword.trim(),
                        ),
                    )
                } catch (e: Exception) {
                    setState(
                        SearchTabState.Error(
                            message = e.message ?: "Unknown Error",
                            searchKeyword = keyword.trim(),
                        ),
                    )
                }
            }
        }
    }
