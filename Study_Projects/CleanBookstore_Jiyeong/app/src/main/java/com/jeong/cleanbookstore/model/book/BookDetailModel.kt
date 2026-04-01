package com.jeong.cleanbookstore.model.book

import androidx.compose.runtime.Immutable

@Immutable
data class BookDetailModel(
    val id: String,
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val thumbnail: String?,
    val previewLink: String?,
    val infoLink: String?,
    val pageCount: Int?,
    val categories: List<String>,
)
