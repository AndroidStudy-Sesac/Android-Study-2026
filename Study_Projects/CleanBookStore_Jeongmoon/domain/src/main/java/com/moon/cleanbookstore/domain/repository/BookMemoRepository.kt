package com.moon.cleanbookstore.domain.repository

import com.moon.cleanbookstore.domain.model.BookMemo
import kotlinx.coroutines.flow.Flow

interface BookMemoRepository {
    fun getBookMemo(id: String): Flow<BookMemo?>
    suspend fun saveBookMemo(bookMemo: BookMemo)
}