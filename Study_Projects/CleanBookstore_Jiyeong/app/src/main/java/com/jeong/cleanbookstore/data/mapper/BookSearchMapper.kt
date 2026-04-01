package com.jeong.cleanbookstore.data.mapper

import com.jeong.cleanbookstore.data.response.BookItemResponse
import com.jeong.cleanbookstore.data.response.BookSearchResultResponse
import com.jeong.cleanbookstore.model.book.BookModel

fun BookItemResponse.toModel(): BookModel =
    BookModel(
        id = id,
        title = volumeInfo.title,
        subtitle = volumeInfo.subtitle,
        authors = volumeInfo.authors,
        publisher = volumeInfo.publisher,
        publishedDate = volumeInfo.publishedDate,
        description = volumeInfo.description,
        thumbnail = volumeInfo.imageLinks?.thumbnail.toHttps(),
        previewLink = volumeInfo.previewLink.toHttps(),
        infoLink = volumeInfo.infoLink.toHttps(),
    )

fun BookSearchResultResponse.toModelList(): List<BookModel> = items?.map { it.toModel() } ?: emptyList()

private fun String?.toHttps(): String? = this?.replace("http://", "https://")
