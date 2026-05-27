package com.subin.cleanbookstore.domain.usecase

import androidx.paging.PagingData
import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.domain.repository.BookSearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSearchBooksUseCase @Inject constructor(
    private val repository: BookSearchRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Book>> {
        return repository.searchBooks(query)
    }
}