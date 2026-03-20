package com.jeong.sesacappjiyeong.feature.auth.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.sesacappjiyeong.feature.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SignUpUiState(
    val nickname: String = "",
    val userId: String = "",
    val userPw: String = "",
    val isLoading: Boolean = false
)

sealed interface SignUpUiEvent {
    object Success : SignUpUiEvent
    data class Error(val message: String) : SignUpUiEvent
}

class SignUpViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<SignUpUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onNicknameChanged(name: String) { _uiState.update { it.copy(nickname = name) } }
    fun onUserIdChanged(id: String) { _uiState.update { it.copy(userId = id) } }
    fun onUserPwChanged(pw: String) { _uiState.update { it.copy(userPw = pw) } }

    fun signUp() {
        val state = _uiState.value
        if (state.nickname.isBlank() || state.userId.isBlank() || state.userPw.isBlank()) {
            viewModelScope.launch { _uiEvent.send(SignUpUiEvent.Error("모든 정보를 입력해주세요.")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (repository.isUserIdExists(state.userId)) {
                _uiEvent.send(SignUpUiEvent.Error("이미 존재하는 아이디입니다."))
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val result = repository.signUp(state.nickname, state.userId, state.userPw)
            _uiState.update { it.copy(isLoading = false) }

            if (result.isSuccess) {
                _uiEvent.send(SignUpUiEvent.Success)
            } else {
                _uiEvent.send(SignUpUiEvent.Error(result.exceptionOrNull()?.message ?: "회원가입 실패"))
            }
        }
    }
}
