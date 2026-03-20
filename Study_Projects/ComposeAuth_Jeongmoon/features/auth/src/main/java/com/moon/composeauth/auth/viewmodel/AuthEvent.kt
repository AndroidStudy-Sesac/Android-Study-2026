package com.moon.composeauth.auth.viewmodel

sealed class AuthEvent {
    data class LoginSuccess(val nickname: String) : AuthEvent()
    object SignUpSuccess : AuthEvent()
    data class ShowSnackbar(val message: String) : AuthEvent()
}