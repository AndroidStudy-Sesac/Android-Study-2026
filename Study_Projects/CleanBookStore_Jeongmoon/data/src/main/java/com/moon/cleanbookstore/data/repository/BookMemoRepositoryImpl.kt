package com.moon.cleanbookstore.data.repository

import com.moon.cleanbookstore.data.local.dao.BookMemoDao
import com.moon.cleanbookstore.data.mapper.toDomain
import com.moon.cleanbookstore.data.mapper.toEntity
import com.moon.cleanbookstore.domain.model.BookMemo
import com.moon.cleanbookstore.domain.repository.BookMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookMemoRepositoryImpl(
    private val bookMemoDao: BookMemoDao
) : BookMemoRepository {

    override fun getBookMemo(id: String): Flow<BookMemo?> {
        return bookMemoDao.getMemoFlow(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveBookMemo(bookMemo: BookMemo) {
        bookMemoDao.insert(bookMemo.toEntity())
    }
}