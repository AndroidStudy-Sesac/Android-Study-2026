package com.moon.cleanbookstore.feature.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moon.cleanbookstore.domain.repository.BookMemoRepository
import com.moon.cleanbookstore.domain.repository.BookSearchRepository
import com.moon.cleanbookstore.domain.repository.BookStoreRepository
import com.moon.cleanbookstore.feature.bookmark.BookmarkTabViewModel
import com.moon.cleanbookstore.feature.detail.BookDetailViewModel
import com.moon.cleanbookstore.feature.home.BookNewTabViewModel
import com.moon.cleanbookstore.feature.search.SearchTabViewModel

class AppViewModelFactory(
    private val bookStoreRepository: BookStoreRepository,
    private val bookSearchRepository: BookSearchRepository,
    private val bookMemoRepository: BookMemoRepository
) : ViewModelProvider.Factory {

    fun createDetailViewModel(bookId: String): BookDetailViewModel {
        return BookDetailViewModel(bookStoreRepository, bookMemoRepository, bookId)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(BookNewTabViewModel::class.java) -> {
                BookNewTabViewModel(bookStoreRepository) as T
            }
            modelClass.isAssignableFrom(SearchTabViewModel::class.java) -> {
                SearchTabViewModel(bookStoreRepository, bookSearchRepository) as T
            }
            modelClass.isAssignableFrom(BookmarkTabViewModel::class.java) -> {
                BookmarkTabViewModel(bookStoreRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}