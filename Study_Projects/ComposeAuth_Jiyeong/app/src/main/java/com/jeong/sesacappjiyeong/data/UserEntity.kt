package com.jeong.sesacappjiyeong.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nickname: String,
    val userId: String,
    val password: String
)
