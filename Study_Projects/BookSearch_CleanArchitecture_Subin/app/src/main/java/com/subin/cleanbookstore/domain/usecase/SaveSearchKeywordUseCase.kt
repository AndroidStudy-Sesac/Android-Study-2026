package com.subin.cleanbookstore.domain.usecase

import com.subin.cleanbookstore.domain.repository.SearchHistoryRepository
import javax.inject.Inject

class SaveSearchKeywordUseCase @Inject constructor(
    private val repository: SearchHistoryRepository
) {
    suspend operator fun invoke(keyword: String) {
        repository.saveSearchKeyword(keyword)
    }
}