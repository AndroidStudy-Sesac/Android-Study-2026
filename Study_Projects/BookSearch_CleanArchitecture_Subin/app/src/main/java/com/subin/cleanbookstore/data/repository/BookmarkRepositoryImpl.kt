package com.subin.cleanbookstore.data.repository

import com.subin.cleanbookstore.data.local.dao.BookmarkDao
import com.subin.cleanbookstore.data.local.entity.BookmarkEntity
import com.subin.cleanbookstore.data.mapper.toDomain
import com.subin.cleanbookstore.data.mapper.toEntity
import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun getAllBookmarks(): Flow<List<Book>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveBookmark(book: Book) {
        bookmarkDao.insertBookmark(book.toEntity())
    }

    override suspend fun deleteBookmark(bookId: String) {
        val dummyEntity = BookmarkEntity(id = bookId, title = "", authors = "", imageUrl = "")
        bookmarkDao.deleteBookmark(dummyEntity)
    }

    override fun isBookmarked(bookId: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(bookId)
    }

    override suspend fun updateMemo(bookId: String, memo: String) {
        bookmarkDao.updateMemo(bookId, memo)
    }
}