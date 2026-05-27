package com.subin.cleanbookstore.domain.model

data class Book(
    val id: String,
    val title: String,
    val subtitle: String,
    val authors: List<String>,
    val publisher: String,
    val description: String,
    val imageUrl: String,
    val buyLink: String,
    val isFavorite: Boolean = false,
    val pdfDownloadLink: String? = null,
    val memo: String = ""
)