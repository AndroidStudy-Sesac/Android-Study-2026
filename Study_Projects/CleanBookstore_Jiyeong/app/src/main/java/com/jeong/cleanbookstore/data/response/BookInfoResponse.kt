package com.jeong.cleanbookstore.data.response

data class BookInfoResponse(
    val title: String = "",
    val subtitle: String? = null,
    val authors: List<String> = emptyList(),
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val categories: List<String> = emptyList(),
    val imageLinks: ImageLinksResponse? = null,
    val previewLink: String? = null,
    val infoLink: String? = null,
)
