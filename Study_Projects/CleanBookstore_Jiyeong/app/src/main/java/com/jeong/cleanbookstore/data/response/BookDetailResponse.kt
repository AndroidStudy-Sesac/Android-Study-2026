package com.jeong.cleanbookstore.data.response

data class BookDetailResponse(
    val id: String = "",
    val volumeInfo: BookInfoResponse = BookInfoResponse(),
    val publisher: String? = null,
)
