package com.jeong.sesacappjiyeong.data

import com.jeong.sesacappjiyeong.feature.auth.domain.AuthRepository
import com.jeong.sesacappjiyeong.feature.auth.domain.AuthUser

class AuthRepositoryImpl(private val userDao: UserDao) : AuthRepository {
    override suspend fun login(userId: String, password: String): AuthUser? {
        return userDao.login(userId, password)?.let {
            AuthUser(it.id, it.nickname, it.userId)
        }
    }

    override suspend fun signUp(nickname: String, userId: String, password: String): Result<Unit> {
        return try {
            userDao.insert(UserEntity(nickname = nickname, userId = userId, password = password))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUserIdExists(userId: String): Boolean {
        return userDao.isUserIdExists(userId)
    }
}
