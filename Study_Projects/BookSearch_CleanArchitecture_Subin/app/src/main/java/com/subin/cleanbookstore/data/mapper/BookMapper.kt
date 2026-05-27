package com.subin.cleanbookstore.data.mapper

import com.subin.cleanbookstore.data.remote.dto.BookDto
import com.subin.cleanbookstore.domain.model.Book

fun BookDto.toDomain(): Book {
    return Book(
        id = id ?: "",
        title = volumeInfo?.title ?: "제목 없음",
        subtitle = volumeInfo?.subtitle ?: "",
        authors = volumeInfo?.authors ?: emptyList(),
        publisher = volumeInfo?.publisher ?: "출판사 정보 없음",
        description = volumeInfo?.description ?: "",
        imageUrl = volumeInfo?.imageLinks?.thumbnail?.replace("http:", "https:") ?: "",
        buyLink = saleInfo?.buyLink ?: "",
        pdfDownloadLink = accessInfoDto?.pdf?.downloadLink
    )
}