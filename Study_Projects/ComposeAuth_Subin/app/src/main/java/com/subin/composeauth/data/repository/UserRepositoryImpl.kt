package com.subin.composeauth.data.repository

import com.subin.composeauth.data.local.UserDao
import com.subin.composeauth.data.local.UserEntity
import com.subin.composeauth.domain.model.User
import com.subin.composeauth.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun registerUser(id: String, pw: String, nickname: String): Boolean {
        return try {
            val entity = UserEntity(id = id, pw = pw, nickname = nickname)
            userDao.insertUser(entity)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun login(id: String, pw: String): User? {
        val userEntity = userDao.getUser(id, pw)
        return if (userEntity != null) {
            User(id = userEntity.id, nickname = userEntity.nickname)
        } else {
            null
        }
    }

    override suspend fun saveAutoLogin(id: String, isAutoLogin: Boolean) {
        if (isAutoLogin) {
            userDao.clearAllAutoLogins()
        }
        userDao.updateAutoLogin(id, isAutoLogin)
    }

    override suspend fun checkAutoLogin(): User? {
        val autoLoginUser = userDao.getAutoLoginUser()
        return if (autoLoginUser != null) {
            User(id = autoLoginUser.id, nickname = autoLoginUser.nickname)
        } else {
            null
        }
    }
}