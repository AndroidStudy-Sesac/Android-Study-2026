package com.moon.composeauth.domain.repository

import com.moon.composeauth.domain.model.User

interface UserRepository {
    suspend fun signUp(id: String, pw: String, nickname: String): Boolean
    suspend fun login(id: String, pw: String): User?
}