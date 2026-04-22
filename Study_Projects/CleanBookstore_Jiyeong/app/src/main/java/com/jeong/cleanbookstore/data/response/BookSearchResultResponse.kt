package com.jeong.cleanbookstore.data.response

data class BookSearchResultResponse(
    val totalItems: Int = 0,
    val items: List<BookItemResponse>? = emptyList(),
)
