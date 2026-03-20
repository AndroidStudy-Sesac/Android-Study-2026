package com.subin.composeauth.presentation.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subin.composeauth.ui.components.CustomButton
import com.subin.composeauth.ui.components.CustomTextField

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.signUpEvent.collect { event ->
            when (event) {
                is SignUpEvent.Success -> {
                    onNavigateBack()
                }
                is SignUpEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    SignUpContent(
        snackbarHostState = snackbarHostState,
        onSignUpClick = { nickname, id, pw -> viewModel.signUp(nickname, id, pw) }
    )
}

@Composable
fun SignUpContent(
    snackbarHostState: SnackbarHostState,
    onSignUpClick: (String, String, String) -> Unit
) {
    var nickname by rememberSaveable { mutableStateOf("") }
    var id by rememberSaveable { mutableStateOf("") }
    var pw by rememberSaveable { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "회원가입", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = "닉네임"
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = id,
                onValueChange = { id = it },
                label = "아이디"
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = pw,
                onValueChange = { pw = it },
                label = "비밀번호",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomButton(onClick = { onSignUpClick(nickname, id, pw) }) {
                Text("가입하기")
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpContent(
            snackbarHostState = remember { SnackbarHostState() },
            onSignUpClick = { _, _, _ -> }
        )
    }
}