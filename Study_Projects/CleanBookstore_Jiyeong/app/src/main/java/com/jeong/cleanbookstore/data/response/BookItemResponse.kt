package com.jeong.cleanbookstore.data.response

data class BookItemResponse(
    val id: String = "",
    val volumeInfo: BookInfoResponse = BookInfoResponse(),
)
