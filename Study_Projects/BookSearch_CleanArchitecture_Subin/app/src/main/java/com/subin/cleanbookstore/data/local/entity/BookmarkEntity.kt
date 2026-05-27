package com.subin.cleanbookstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: String,
    val imageUrl: String,
    val addedAt: Long = System.currentTimeMillis(),
    val memo: String = ""
)