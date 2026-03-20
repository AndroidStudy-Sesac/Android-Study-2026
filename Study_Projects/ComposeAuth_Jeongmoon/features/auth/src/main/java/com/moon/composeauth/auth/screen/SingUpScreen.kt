package com.moon.composeauth.auth.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moon.composeauth.auth.R
import com.moon.composeauth.auth.viewmodel.AuthEvent
import com.moon.composeauth.auth.viewmodel.AuthViewModel
import com.moon.composeauth.ui.components.AppButton
import com.moon.composeauth.ui.components.AppTextField
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpRoute(
    viewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onSignUpSuccess: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val nickname by viewModel.nicknameText.collectAsStateWithLifecycle()
    val email by viewModel.idText.collectAsStateWithLifecycle()
    val pass by viewModel.pwText.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.authEvent) {
        viewModel.authEvent.collectLatest { event ->
            when (event) {
                is AuthEvent.SignUpSuccess -> onSignUpSuccess()
                is AuthEvent.ShowSnackbar -> onShowSnackbar(event.message)
                else -> {}
            }
        }
    }

    SignUpScreen(
        nickname = nickname,
        email = email,
        pass = pass,
        onNicknameChange = viewModel::updateNickname,
        onEmailChange = viewModel::updateId,
        onPassChange = viewModel::updatePw,
        onSignUpClick = viewModel::signUp,
        onBackClick = onBackClick
    )
}

@Composable
fun SignUpScreen(
    nickname: String,
    email: String,
    pass: String,
    onNicknameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
        Text(
            text = stringResource(R.string.member_create),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        AppTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = stringResource(R.string.nick_name)
        )
        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.email)
        )
        AppTextField(
            value = pass,
            onValueChange = onPassChange,
            label = stringResource(R.string.password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, "") }
            }
        )
        AppButton(text = stringResource(R.string.member_insert), onClick = onSignUpClick)
    }
}

@Preview(showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen("문", "test@test.com", "", {}, {}, {}, {}, {})
}