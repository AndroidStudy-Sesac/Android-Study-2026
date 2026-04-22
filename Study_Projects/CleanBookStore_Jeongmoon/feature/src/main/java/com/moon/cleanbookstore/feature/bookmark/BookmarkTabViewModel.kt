package com.moon.cleanbookstore.feature.bookmark

import androidx.lifecycle.viewModelScope
import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.domain.repository.BookStoreRepository
import com.moon.cleanbookstore.feature.base.BaseViewModel
import com.moon.cleanbookstore.feature.base.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class BookmarkTabViewModel(
    private val bookStoreRepository: BookStoreRepository
) : BaseViewModel<BookmarkState, Event>() {

    override fun getInitialState(): BookmarkState = BookmarkState.Uninitialized

    override fun fetchData(): Job = viewModelScope.launch {
        setState(BookmarkState.Loading)

        bookStoreRepository.getBookmarkedBooks()
            .catch { e -> setState(BookmarkState.Error(e.message ?: "Unknown Error")) }
            .collect { bookmarkedList ->
                setState(BookmarkState.Success(books = bookmarkedList))
            }
    }

    fun removeBookmark(book: Book) {
        viewModelScope.launch {
            bookStoreRepository.removeBookFromBookmark(book.id)
        }
    }
}