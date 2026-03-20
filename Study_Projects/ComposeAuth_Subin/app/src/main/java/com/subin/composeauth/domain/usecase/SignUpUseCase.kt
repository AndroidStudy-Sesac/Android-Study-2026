package com.subin.composeauth.domain.usecase

import com.subin.composeauth.domain.repository.UserRepository

class SignUpUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(nickname: String, id: String, pw: String): Result<Unit> {
        if (nickname.isBlank() || id.isBlank() || pw.isBlank()) {
            return Result.failure(IllegalArgumentException("모든 항목을 입력해주세요."))
        }

        if (pw.length < 4) {
            return Result.failure(IllegalArgumentException("비밀번호는 4자리 이상이어야 합니다."))
        }

        val isSuccess = userRepository.registerUser(id = id, pw = pw, nickname = nickname)

        return if (isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("회원가입에 실패했습니다. 아이디가 중복되었을 수 있습니다."))
        }
    }
}