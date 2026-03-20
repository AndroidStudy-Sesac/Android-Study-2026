package com.subin.composeauth.domain.usecase

import com.subin.composeauth.domain.model.User
import com.subin.composeauth.domain.repository.UserRepository

class LoginUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String, pw: String, isAutoLogin: Boolean): Result<User> {
        if (id.isBlank() || pw.isBlank()) {
            return Result.failure(Exception("아이디와 비밀번호를 모두 입력해주세요."))
        }

        val user = userRepository.login(id, pw)

        return if (user != null) {
            userRepository.saveAutoLogin(user.id, isAutoLogin)
            Result.success(user)
        } else {
            Result.failure(Exception("아이디 또는 비밀번호가 일치하지 않습니다."))
        }
    }
}