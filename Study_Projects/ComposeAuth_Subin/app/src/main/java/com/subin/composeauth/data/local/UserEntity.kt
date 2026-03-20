package com.subin.composeauth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val pw: String,
    val nickname: String,
    val isAutoLogin: Boolean = false
)