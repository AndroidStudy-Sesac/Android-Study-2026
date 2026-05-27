package com.subin.cleanbookstore.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.domain.usecase.DeleteSearchHistoryUseCase
import com.subin.cleanbookstore.domain.usecase.GetBookmarkListUseCase
import com.subin.cleanbookstore.domain.usecase.GetRecentSearchHistoryUseCase
import com.subin.cleanbookstore.domain.usecase.GetSearchBooksUseCase
import com.subin.cleanbookstore.domain.usecase.SaveSearchKeywordUseCase
import com.subin.cleanbookstore.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSearchViewModel @Inject constructor(
    private val getSearchBooksUseCase: GetSearchBooksUseCase,
    private val getBookmarkListUseCase: GetBookmarkListUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val getRecentSearchHistoryUseCase: GetRecentSearchHistoryUseCase,
    private val saveSearchKeywordUseCase: SaveSearchKeywordUseCase,
    private val deleteSearchHistoryUseCase: DeleteSearchHistoryUseCase
) : ViewModel() {

    val recentSearchHistory: StateFlow<List<String>> = getRecentSearchHistoryUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchPagingData: Flow<PagingData<Book>> = _searchQuery
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            getSearchBooksUseCase(query)
        }
        .cachedIn(viewModelScope)
        .combine(getBookmarkListUseCase()) { pagingData, bookmarks ->
            pagingData.map { book ->
                book.copy(isFavorite = bookmarks.any { it.id == book.id })
            }
        }

    fun searchBooks(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            saveSearchKeywordUseCase(query)
        }
        _searchQuery.value = query
    }

    fun resetSearchState() {
        _searchQuery.value = ""
    }


    fun deleteHistory(keyword: String) {
        viewModelScope.launch {
            deleteSearchHistoryUseCase.deleteKeyword(keyword)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            deleteSearchHistoryUseCase.clearAll()
        }
    }

    fun onBookmarkClick(book: Book) {
        viewModelScope.launch {
            toggleBookmarkUseCase(book)
        }
    }
}