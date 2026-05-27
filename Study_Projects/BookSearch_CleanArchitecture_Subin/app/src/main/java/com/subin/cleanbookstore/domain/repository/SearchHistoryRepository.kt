package com.subin.cleanbookstore.domain.repository

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getRecentSearchHistory(): Flow<List<String>>
    suspend fun saveSearchKeyword(keyword: String)
    suspend fun deleteSearchKeyword(keyword: String)
    suspend fun clearAllHistory()
}