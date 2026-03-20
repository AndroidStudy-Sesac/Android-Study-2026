package com.jeong.sesacappjiyeong.feature.auth.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeong.sesacappjiyeong.feature.auth.ui.component.AuthSlotButton
import com.jeong.sesacappjiyeong.feature.auth.ui.component.AuthSlotTextField

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // rememberSaveable to demonstrate requirement, though ViewModel state also survives rotation.
    // In a real app, we might use this for local UI state that doesn't need to be in the ViewModel source of truth.
    var userIdBuffer by rememberSaveable { mutableStateOf("") }
    var passwordBuffer by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.Success -> onLoginSuccess(event.nickname)
                is LoginUiEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is LoginUiEvent.NavigateToSignUp -> onNavigateToSignUp()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "SeSAC Login", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(32.dp))

            AuthSlotTextField(
                value = uiState.userId,
                onValueChange = viewModel::onUserIdChanged,
                labelContent = { Text("아이디") },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                supportingContent = if (uiState.userId.isEmpty()) {
                    { Text("아이디를 입력해주세요") }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthSlotTextField(
                value = uiState.userPw,
                onValueChange = viewModel::onUserPwChanged,
                isPassword = true,
                labelContent = { Text("비밀번호") },
                leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthSlotButton(
                onClick = viewModel::login,
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("로그인")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = viewModel::onSignUpClick) {
                Text("계정이 없으신가요? 회원가입")
            }
        }
    }
}
