package com.jeong.sesacappjiyeong.feature.auth.domain

interface AuthRepository {
    suspend fun login(userId: String, password: String): AuthUser?
    suspend fun signUp(nickname: String, userId: String, password: String): Result<Unit>
    suspend fun isUserIdExists(userId: String): Boolean
}
