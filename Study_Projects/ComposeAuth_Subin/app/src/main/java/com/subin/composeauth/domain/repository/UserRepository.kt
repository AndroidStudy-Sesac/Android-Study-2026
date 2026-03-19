package com.subin.composeauth.domain.repository

import com.subin.composeauth.domain.model.User

interface UserRepository {
    suspend fun registerUser(id: String, pw: String, nickname: String): Boolean
    suspend fun login(id: String, pw: String): User?

    suspend fun saveAutoLogin(id: String, isAutoLogin: Boolean)
    suspend fun checkAutoLogin(): User?
}