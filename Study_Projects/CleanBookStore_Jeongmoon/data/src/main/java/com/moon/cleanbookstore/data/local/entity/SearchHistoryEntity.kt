package com.moon.cleanbookstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val searchKeyword: String,
    val searchTimestamp: Long
)