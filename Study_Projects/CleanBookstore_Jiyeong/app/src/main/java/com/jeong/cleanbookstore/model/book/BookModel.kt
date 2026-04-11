package com.jeong.cleanbookstore.model.book

import androidx.compose.runtime.Immutable

/**
 * Book model
 *
 * @Immutable: `BookModel`이 변경되지 않았으면 recomposition이 일어나지 않음
 */
@Immutable
data class BookModel(
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
    val isLiked: Boolean? = null,
    val pageCount: Int? = null,
    val categories: List<String> = emptyList(),
)
