package com.subin.cleanbookstore.domain.usecase

import com.subin.cleanbookstore.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentSearchHistoryUseCase @Inject constructor(
    private val repository: SearchHistoryRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return repository.getRecentSearchHistory()
    }
}