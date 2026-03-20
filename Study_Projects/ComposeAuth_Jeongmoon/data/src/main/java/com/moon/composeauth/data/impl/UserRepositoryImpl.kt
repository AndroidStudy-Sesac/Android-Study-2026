package com.moon.composeauth.data.impl

import com.moon.composeauth.data.dao.UserDao
import com.moon.composeauth.data.entity.UserEntity
import com.moon.composeauth.domain.model.User
import com.moon.composeauth.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun signUp(id: String, pw: String, nickname: String): Boolean {
        val entity = UserEntity(userId = id, password = pw, nickname = nickname)
        val result = userDao.insertUser(entity)
        return result != -1L
    }

    override suspend fun login(id: String, pw: String): User? {
        val entity = userDao.getUserById(id)
        return if (entity != null && entity.password == pw) {
            User(id = entity.userId, nickname = entity.nickname)
        } else {
            null
        }
    }
}