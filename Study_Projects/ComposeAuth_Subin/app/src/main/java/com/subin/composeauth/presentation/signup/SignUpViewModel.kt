package com.subin.composeauth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subin.composeauth.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class SignUpEvent {
    object Success : SignUpEvent()
    data class Error(val message: String) : SignUpEvent()
}

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _signUpEvent = MutableSharedFlow<SignUpEvent>()
    val signUpEvent = _signUpEvent.asSharedFlow()

    fun signUp(nickname: String, id: String, pw: String) {
        viewModelScope.launch {
            signUpUseCase(nickname, id, pw).fold(
                onSuccess = {
                    _signUpEvent.emit(SignUpEvent.Success)
                },
                onFailure = { exception ->
                    _signUpEvent.emit(SignUpEvent.Error(exception.message ?: "알 수 없는 에러가 발생했습니다."))
                }
            )
        }
    }
}