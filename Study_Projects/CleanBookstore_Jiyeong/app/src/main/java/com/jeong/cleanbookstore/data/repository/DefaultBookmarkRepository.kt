package com.jeong.cleanbookstore.data.repository

import com.jeong.cleanbookstore.data.db.dao.BookmarkDao
import com.jeong.cleanbookstore.data.mapper.toBookmarkEntity
import com.jeong.cleanbookstore.data.mapper.toModel
import com.jeong.cleanbookstore.model.book.BookModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultBookmarkRepository
    @Inject
    constructor(
        private val bookmarkDao: BookmarkDao,
    ) : BookmarkRepository {
        override fun getBookmarks(): Flow<List<BookModel>> = bookmarkDao.getAll().map { list -> list.map { it.toModel() } }

        override fun getBookmark(id: String): Flow<BookModel?> = bookmarkDao.get(id).map { entity -> entity?.toModel() }

        override suspend fun addBookmark(book: BookModel) {
            bookmarkDao.insert(book.toBookmarkEntity())
        }

        override suspend fun removeBookmark(id: String) {
            bookmarkDao.delete(id)
        }
    }
