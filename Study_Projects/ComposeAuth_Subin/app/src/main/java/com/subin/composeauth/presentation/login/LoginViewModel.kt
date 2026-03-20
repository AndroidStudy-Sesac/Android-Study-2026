package com.subin.composeauth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subin.composeauth.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class LoginEvent {
    data class Success(val nickname: String) : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    fun login(id: String, pw: String, isAutoLogin: Boolean) {
        viewModelScope.launch {
            loginUseCase(id, pw, isAutoLogin).fold(
                onSuccess = { user ->
                    _loginEvent.emit(LoginEvent.Success(user.nickname))
                },
                onFailure = { exception ->
                    _loginEvent.emit(LoginEvent.Error(exception.message ?: "알 수 없는 에러가 발생했습니다."))
                }
            )
        }
    }
}