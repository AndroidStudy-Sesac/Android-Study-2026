package com.moon.cleanbookstore.feature.search

import androidx.lifecycle.viewModelScope
import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.domain.model.SearchHistory
import com.moon.cleanbookstore.domain.repository.BookSearchRepository
import com.moon.cleanbookstore.domain.repository.BookStoreRepository
import com.moon.cleanbookstore.feature.base.BaseViewModel
import com.moon.cleanbookstore.feature.base.Event
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SearchTabViewModel(
    private val bookStoreRepository: BookStoreRepository,
    private val searchRepository: BookSearchRepository
) : BaseViewModel<SearchTabState, Event>() {

    override fun getInitialState(): SearchTabState = SearchTabState.Uninitialized

    init {
        loadSearchHistory()
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            searchRepository.getAllSearchHistories()
                .catch { }
                .collect { historyList ->
                    if (stateFlow.value !is SearchTabState.Success) {
                        val keywords = historyList.map { it.keyword }
                        setState(SearchTabState.History(keywords = keywords))
                    }
                }
        }
    }

    fun searchBooks(keyword: String) {
        if (keyword.isBlank()) return

        setState(SearchTabState.Loading)

        viewModelScope.launch {
            searchRepository.saveSearchHistory(
                SearchHistory(
                    keyword = keyword,
                    timestamp = System.currentTimeMillis()
                )
            )

            combine(
                bookStoreRepository.searchBooks(keyword, 0),
                bookStoreRepository.getBookmarkedBooks()
            ) { searchResults, wishList ->
                searchResults.map { book ->
                    book.copy(isBookmarked = wishList.any { it.id == book.id })
                }
            }
                .catch { e -> setState(SearchTabState.Error(e.message ?: "Unknown Error")) }
                .collect { bookList ->
                    setState(SearchTabState.Success(books = bookList, currentKeyword = keyword))
                }
        }
    }

    fun removeHistory(keyword: String) {
        viewModelScope.launch {
            searchRepository.deleteSearchHistory(keyword)
        }
    }

    fun toggleBookmark(book: Book) {
        viewModelScope.launch {
            if (book.isBookmarked) {
                bookStoreRepository.removeBookFromBookmark(book.id)
            } else {
                bookStoreRepository.addBookToBookmark(book.copy(isBookmarked = true))
            }

            withState<SearchTabState.Success> { state ->
                val updatedBooks = state.books.map {
                    if (it.id == book.id) it.copy(isBookmarked = !it.isBookmarked) else it
                }
                setState(state.copy(books = updatedBooks))
            }
        }
    }
}