package com.moon.cleanbookstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_memo")
data class BookMemoEntity(
    @PrimaryKey val id: String,
    val memo: String,
    val lastModified: Long
)