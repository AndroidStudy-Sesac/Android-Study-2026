package com.moon.cleanbookstore.data.mapper

import com.moon.cleanbookstore.data.local.entity.BookEntity
import com.moon.cleanbookstore.data.local.entity.BookMemoEntity
import com.moon.cleanbookstore.data.local.entity.SearchHistoryEntity
import com.moon.cleanbookstore.data.remote.response.BookItemDto
import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.domain.model.BookMemo
import com.moon.cleanbookstore.domain.model.SearchHistory

fun BookItemDto.toDomain(isBookmarked: Boolean = false): Book {
    return Book(
        id = this.id,
        title = this.volumeInfo?.title ?: "",
        subtitle = this.volumeInfo?.subtitle ?: "",
        authors = this.volumeInfo?.authors ?: emptyList(),
        description = this.volumeInfo?.description ?: "",
        imageUrl = this.volumeInfo?.imageLinks?.thumbnail?.replace("http:", "https:") ?: "",
        pdfLink = this.accessInfo?.pdf?.acsTokenLink ?: "",
        isBookmarked = isBookmarked
    )
}

fun BookEntity.toDomain(): Book {
    return Book(
        id = this.id,
        title = this.title,
        subtitle = this.subtitle,
        authors = if (this.authors.isNotBlank()) this.authors.split(",") else emptyList(),
        description = this.description,
        imageUrl = this.imageUrl,
        pdfLink = this.pdfLink,
        isBookmarked = true
    )
}

fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = this.id,
        title = this.title,
        subtitle = this.subtitle,
        authors = this.authors.joinToString(","),
        description = this.description,
        imageUrl = this.imageUrl,
        pdfLink = this.pdfLink
    )
}

fun BookMemoEntity.toDomain() = BookMemo(id, memo, lastModified)
fun BookMemo.toEntity() = BookMemoEntity(id, memoContent, lastModified)

fun SearchHistoryEntity.toDomain() = SearchHistory(searchKeyword, searchTimestamp)
fun SearchHistory.toEntity() = SearchHistoryEntity(keyword, timestamp)