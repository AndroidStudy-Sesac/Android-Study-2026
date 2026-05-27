package com.subin.cleanbookstore.data.repository

import com.subin.cleanbookstore.data.local.dao.SearchHistoryDao
import com.subin.cleanbookstore.data.local.entity.SearchHistoryEntity
import com.subin.cleanbookstore.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override fun getRecentSearchHistory(): Flow<List<String>> {
        return searchHistoryDao.getRecentSearchHistory().map { entities ->
            entities.map { it.keyword }
        }
    }

    override suspend fun saveSearchKeyword(keyword: String) {
        if (keyword.isNotBlank()) {
            val entity = SearchHistoryEntity(
                keyword = keyword.trim(),
                timestamp = System.currentTimeMillis()
            )
            searchHistoryDao.insertKeyword(entity)
        }
    }

    override suspend fun deleteSearchKeyword(keyword: String) {
        searchHistoryDao.deleteKeyword(keyword)
    }

    override suspend fun clearAllHistory() {
        searchHistoryDao.deleteAllHistory()
    }
}