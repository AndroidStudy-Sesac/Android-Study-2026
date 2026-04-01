package com.jeong.cleanbookstore.data.mapper

import android.text.Html
import com.jeong.cleanbookstore.data.response.BookDetailResponse
import com.jeong.cleanbookstore.model.book.BookDetailModel

fun BookDetailResponse.toDetailModel(): BookDetailModel =
    BookDetailModel(
        id = id,
        title = volumeInfo.title,
        subtitle = volumeInfo.subtitle,
        authors = volumeInfo.authors,
        publisher = volumeInfo.publisher,
        publishedDate = volumeInfo.publishedDate,
        description = volumeInfo.description.toPlainText(),
        thumbnail = volumeInfo.imageLinks?.thumbnail.toHttps(),
        previewLink = volumeInfo.previewLink.toHttps(),
        infoLink = volumeInfo.infoLink.toHttps(),
        pageCount = volumeInfo.pageCount,
        categories = volumeInfo.categories,
    )

private fun String?.toHttps(): String? = this?.replace("http://", "https://")

private fun String?.toPlainText(): String {
    if (this.isNullOrBlank()) return ""
    return Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString()
}
