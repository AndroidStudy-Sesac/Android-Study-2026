package com.moon.cleanbookstore.feature.home

import androidx.lifecycle.viewModelScope
import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.domain.repository.BookStoreRepository
import com.moon.cleanbookstore.feature.base.BaseViewModel
import com.moon.cleanbookstore.feature.base.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BookNewTabViewModel(
    private val bookStoreRepository: BookStoreRepository
) : BaseViewModel<NewTabState, Event>() {

    override fun getInitialState(): NewTabState = NewTabState.Uninitialized

    override fun fetchData(): Job = viewModelScope.launch {
        setState(NewTabState.Loading)

        combine(
            bookStoreRepository.searchBooks("new", 0),
            bookStoreRepository.getBookmarkedBooks()
        ) { newBooks, wishList ->
            newBooks.map { book ->
                book.copy(isBookmarked = wishList.any { it.id == book.id })
            }
        }
            .catch { e -> setState(NewTabState.Error(e.message ?: "Unknown Error")) }
            .collect { bookList ->
                setState(NewTabState.Success(books = bookList))
            }
    }

    fun toggleBookmark(book: Book) {
        viewModelScope.launch {
            if (book.isBookmarked) {
                bookStoreRepository.removeBookFromBookmark(book.id)
            } else {
                bookStoreRepository.addBookToBookmark(book.copy(isBookmarked = true))
            }
        }
    }
}