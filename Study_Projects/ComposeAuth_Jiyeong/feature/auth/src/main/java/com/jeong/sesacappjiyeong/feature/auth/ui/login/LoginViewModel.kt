package com.jeong.sesacappjiyeong.feature.auth.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.sesacappjiyeong.feature.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LoginUiState(
    val userId: String = "",
    val userPw: String = "",
    val isLoading: Boolean = false
)

sealed interface LoginUiEvent {
    data class Success(val nickname: String) : LoginUiEvent
    data class Error(val message: String) : LoginUiEvent
    object NavigateToSignUp : LoginUiEvent
}

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<LoginUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onUserIdChanged(id: String) { _uiState.update { it.copy(userId = id) } }
    fun onUserPwChanged(pw: String) { _uiState.update { it.copy(userPw = pw) } }

    fun login() {
        val state = _uiState.value
        if (state.userId.isBlank() || state.userPw.isBlank()) {
            viewModelScope.launch { _uiEvent.send(LoginUiEvent.Error("아이디와 비밀번호를 입력해주세요.")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = repository.login(state.userId, state.userPw)
            _uiState.update { it.copy(isLoading = false) }

            if (user != null) {
                _uiEvent.send(LoginUiEvent.Success(user.nickname))
            } else {
                _uiEvent.send(LoginUiEvent.Error("아이디 또는 비밀번호가 일치하지 않습니다."))
            }
        }
    }

    fun onSignUpClick() {
        viewModelScope.launch { _uiEvent.send(LoginUiEvent.NavigateToSignUp) }
    }
}
