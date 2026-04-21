package com.moon.cleanbookstore.feature.detail

import androidx.lifecycle.viewModelScope
import com.moon.cleanbookstore.domain.model.BookMemo
import com.moon.cleanbookstore.domain.repository.BookMemoRepository
import com.moon.cleanbookstore.domain.repository.BookStoreRepository
import com.moon.cleanbookstore.feature.base.BaseViewModel
import com.moon.cleanbookstore.feature.base.Event
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BookDetailViewModel(
    private val bookStoreRepository: BookStoreRepository,
    private val bookMemoRepository: BookMemoRepository,
    private val bookId: String
) : BaseViewModel<BookDetailState, Event>() {

    override fun getInitialState(): BookDetailState = BookDetailState.Uninitialized

    init {
        fetchData()
    }

    override fun fetchData() = viewModelScope.launch {
        setState(BookDetailState.Loading)

        combine(
            bookStoreRepository.getBookDetail(bookId),
            bookStoreRepository.getBookmarkedBook(bookId),
            bookMemoRepository.getBookMemo(bookId)
        ) { detail, bookmark, memo ->
            val bookWithBookmark = detail.copy(isBookmarked = bookmark != null)
            BookDetailState.Success(book = bookWithBookmark, memo = memo)
        }
            .catch { e -> setState(BookDetailState.Error(e.message ?: "Unknown Error")) }
            .collect { successState ->
                setState(successState)
            }
    }

    fun toggleBookmark() {
        withState<BookDetailState.Success> { state ->
            viewModelScope.launch {
                if (state.book.isBookmarked) {
                    bookStoreRepository.removeBookFromBookmark(bookId)
                } else {
                    bookStoreRepository.addBookToBookmark(state.book.copy(isBookmarked = true))
                }
            }
        }
    }

    fun saveMemo(content: String) {
        viewModelScope.launch {
            bookMemoRepository.saveBookMemo(BookMemo(id = bookId, memoContent = content, lastModified = System.currentTimeMillis()))
        }
    }
}