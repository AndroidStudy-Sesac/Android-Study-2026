package com.moon.cleanbookstore.data.repository

import com.moon.cleanbookstore.data.local.dao.SearchHistoryDao
import com.moon.cleanbookstore.data.local.entity.SearchHistoryEntity
import com.moon.cleanbookstore.data.mapper.toDomain
import com.moon.cleanbookstore.data.mapper.toEntity
import com.moon.cleanbookstore.domain.model.SearchHistory
import com.moon.cleanbookstore.domain.repository.BookSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class BookSearchRepositoryImpl(
    private val searchHistoryDao: SearchHistoryDao
) : BookSearchRepository {

    override fun getAllSearchHistories(): Flow<List<SearchHistory>> {
        return searchHistoryDao.getAllFlow().map { entities: List<SearchHistoryEntity> ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveSearchHistory(searchHistory: SearchHistory) {
        searchHistoryDao.insert(searchHistory.toEntity())
    }

    override suspend fun deleteSearchHistory(keyword: String) {
        searchHistoryDao.delete(keyword)
    }
}