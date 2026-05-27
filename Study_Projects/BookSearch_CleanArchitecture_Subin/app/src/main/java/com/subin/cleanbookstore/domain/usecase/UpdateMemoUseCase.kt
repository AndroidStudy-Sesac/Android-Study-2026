package com.subin.cleanbookstore.domain.usecase

import com.subin.cleanbookstore.domain.repository.BookmarkRepository
import javax.inject.Inject

class UpdateMemoUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(bookId: String, memo: String) {
        repository.updateMemo(bookId, memo)
    }
}