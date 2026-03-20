package com.jeong.sesacappjiyeong.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId AND password = :password LIMIT 1")
    suspend fun login(userId: String, password: String): UserEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE userId = :userId)")
    suspend fun isUserIdExists(userId: String): Boolean
}
