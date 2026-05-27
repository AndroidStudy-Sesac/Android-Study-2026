package com.subin.cleanbookstore.data.mapper

import com.subin.cleanbookstore.data.local.entity.BookmarkEntity
import com.subin.cleanbookstore.domain.model.Book

fun BookmarkEntity.toDomain() = Book(
    id = id,
    title = title,
    authors = authors.split(", "),
    imageUrl = imageUrl,
    subtitle = "",
    description = "",
    publisher = "",
    buyLink = "",
    isFavorite = true,
    memo = memo
)

fun Book.toEntity() = BookmarkEntity(
    id = id,
    title = title,
    authors = authors.joinToString(", "),
    imageUrl = imageUrl,
    memo = memo
)