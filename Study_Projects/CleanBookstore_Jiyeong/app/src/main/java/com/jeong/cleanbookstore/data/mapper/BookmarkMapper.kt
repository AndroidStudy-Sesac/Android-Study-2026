package com.jeong.cleanbookstore.data.mapper

import com.jeong.cleanbookstore.data.entity.BookmarkEntity
import com.jeong.cleanbookstore.model.book.BookModel

fun BookmarkEntity.toModel(): BookModel =
    BookModel(
        id = id,
        title = title,
        subtitle = subtitle,
        authors = authors.split(", ").filter { it.isNotBlank() },
        publisher = publisher,
        publishedDate = publishedDate,
        description = description,
        thumbnail = thumbnail,
        previewLink = previewLink,
        infoLink = infoLink,
        isLiked = true,
    )

fun BookModel.toBookmarkEntity(): BookmarkEntity =
    BookmarkEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        authors = authors.joinToString(),
        publisher = publisher,
        publishedDate = publishedDate,
        description = description,
        thumbnail = thumbnail,
        previewLink = previewLink,
        infoLink = infoLink,
    )
