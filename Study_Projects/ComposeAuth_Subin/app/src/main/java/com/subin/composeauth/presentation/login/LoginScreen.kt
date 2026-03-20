package com.subin.composeauth.presentation.login

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
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Checkbox

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToMain: (String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loginEvent.collect { event ->
            when (event) {
                is LoginEvent.Success -> {
                    onNavigateToMain(event.nickname)
                }
                is LoginEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LoginContent(
        snackbarHostState = snackbarHostState,
        onLoginClick = { id, pw, isAutoLogin ->
            viewModel.login(id, pw, isAutoLogin)
        },
        onNavigateToSignUp = onNavigateToSignUp
    )
}

@Composable
fun LoginContent(
    snackbarHostState: SnackbarHostState,
    onLoginClick: (String, String, Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var id by rememberSaveable { mutableStateOf("") }
    var pw by rememberSaveable { mutableStateOf("") }
    var isAutoLogin by rememberSaveable { mutableStateOf(false) }

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
            Text(text = "로그인", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(32.dp))

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(text = "자동 로그인", style = MaterialTheme.typography.bodyMedium)
                Checkbox(
                    checked = isAutoLogin,
                    onCheckedChange = { isAutoLogin = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomButton(onClick = { onLoginClick(id, pw, isAutoLogin) }) {
                Text("로그인")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToSignUp) {
                Text("회원가입 하러가기")
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginContent(
            snackbarHostState = remember { SnackbarHostState() },
            onLoginClick = { _, _, _ -> }, // Preview 맞춰주기
            onNavigateToSignUp = {}
        )
    }
}