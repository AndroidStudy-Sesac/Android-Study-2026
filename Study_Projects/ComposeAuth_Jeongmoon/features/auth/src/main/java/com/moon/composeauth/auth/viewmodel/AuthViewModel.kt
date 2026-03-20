package com.moon.composeauth.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.composeauth.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _idText = MutableStateFlow("")
    val idText: StateFlow<String> = _idText.asStateFlow()

    private val _pwText = MutableStateFlow("")
    val pwText: StateFlow<String> = _pwText.asStateFlow()

    private val _nicknameText = MutableStateFlow("")
    val nicknameText: StateFlow<String> = _nicknameText.asStateFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    fun updateId(id: String) { _idText.value = id }
    fun updatePw(pw: String) { _pwText.value = pw }
    fun updateNickname(nickname: String) { _nicknameText.value = nickname }

    fun login() {
        viewModelScope.launch {
            val user = userRepository.login(_idText.value, _pwText.value)
            if (user != null) {
                _authEvent.emit(AuthEvent.LoginSuccess(user.nickname))
            } else {
                _authEvent.emit(AuthEvent.ShowSnackbar("아이디 또는 비밀번호가 틀렸습니다."))
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            val success = userRepository.signUp(_idText.value, _pwText.value, _nicknameText.value)
            if (success) {
                _authEvent.emit(AuthEvent.SignUpSuccess)
            } else {
                _authEvent.emit(AuthEvent.ShowSnackbar("이미 존재하는 아이디입니다."))
            }
        }
    }
}