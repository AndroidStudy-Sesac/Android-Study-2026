package com.subin.cleanbookstore.domain.usecase

import com.subin.cleanbookstore.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class DeleteSearchHistoryUseCase @Inject constructor(
    private val repository: SearchHistoryRepository
) {
    suspend fun deleteKeyword(keyword: String) {
        repository.deleteSearchKeyword(keyword)
    }

    suspend fun clearAll() {
        repository.clearAllHistory()
    }
}