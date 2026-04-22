package com.moon.cleanbookstore.data.repository

import com.moon.cleanbookstore.data.local.dao.BookInWishListDao
import com.moon.cleanbookstore.data.mapper.toDomain
import com.moon.cleanbookstore.data.mapper.toEntity
import com.moon.cleanbookstore.data.remote.api.BooksApiService
import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.domain.repository.BookStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class BookStoreRepositoryImpl(
    private val apiService: BooksApiService,
    private val bookInWishListDao: BookInWishListDao
) : BookStoreRepository {

    override fun searchBooks(keyword: String, startIndex: Int): Flow<List<Book>> = flow {
        val response = apiService.searchBooks(query = keyword, startIndex = startIndex)
        val domainBooks = response.items?.map { it.toDomain() } ?: emptyList()
        emit(domainBooks)
    }

    override fun getBookDetail(id: String): Flow<Book> = flow {
        val response = apiService.getBookInfo(id)
        emit(response.toDomain())
    }

    override fun getBookmarkedBooks(): Flow<List<Book>> {
        return bookInWishListDao.getAllBookmarksFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBookmarkedBook(id: String): Flow<Book?> {
        return bookInWishListDao.getBookmarkFlow(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun addBookToBookmark(book: Book) {
        bookInWishListDao.insert(book.toEntity())
    }

    override suspend fun removeBookFromBookmark(id: String) {
        bookInWishListDao.delete(id)
    }
}