package com.moon.cleanbookstore.domain.repository

import com.moon.cleanbookstore.domain.model.SearchHistory
import kotlinx.coroutines.flow.Flow

interface BookSearchRepository {
    fun getAllSearchHistories(): Flow<List<SearchHistory>>
    suspend fun saveSearchHistory(searchHistory: SearchHistory)
    suspend fun deleteSearchHistory(keyword: String)
}