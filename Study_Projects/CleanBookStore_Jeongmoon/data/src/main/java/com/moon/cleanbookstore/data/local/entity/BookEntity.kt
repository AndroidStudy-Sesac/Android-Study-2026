package com.moon.cleanbookstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_wish_list")
data class BookEntity(
    @PrimaryKey val id: String, // Google Books ID
    val title: String,
    val subtitle: String,
    val authors: String,
    val description: String,
    val imageUrl: String,
    val pdfLink: String
)