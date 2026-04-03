package com.jeong.cleanbookstore.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String?,
    val authors: String,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val thumbnail: String?,
    val previewLink: String?,
    val infoLink: String?,
)
