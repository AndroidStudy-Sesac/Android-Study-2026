package com.subin.composeauth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id AND pw = :pw LIMIT 1")
    suspend fun getUser(id: String, pw: String): UserEntity?

    // 모든 유저의 자동 로그인 상태를 false로 초기화
    @Query("UPDATE users SET isAutoLogin = 0")
    suspend fun clearAllAutoLogins()

    // 특정 유저의 자동 로그인 상태 업데이트
    @Query("UPDATE users SET isAutoLogin = :isAutoLogin WHERE id = :id")
    suspend fun updateAutoLogin(id: String, isAutoLogin: Boolean)

    // 앱을 켤 때 자동 로그인이 켜져 있는 유저를 찾음
    @Query("SELECT * FROM users WHERE isAutoLogin = 1 LIMIT 1")
    suspend fun getAutoLoginUser(): UserEntity?
}