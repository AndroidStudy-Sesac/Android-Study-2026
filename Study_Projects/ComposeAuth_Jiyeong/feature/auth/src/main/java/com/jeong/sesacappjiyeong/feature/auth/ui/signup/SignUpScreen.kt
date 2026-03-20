package com.jeong.sesacappjiyeong.feature.auth.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeong.sesacappjiyeong.feature.auth.ui.component.AuthSlotButton
import com.jeong.sesacappjiyeong.feature.auth.ui.component.AuthSlotTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onSignUpSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SignUpUiEvent.Success -> onSignUpSuccess()
                is SignUpUiEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원가입") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Back") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthSlotTextField(
                value = uiState.nickname,
                onValueChange = viewModel::onNicknameChanged,
                labelContent = { Text("닉네임") },
                leadingContent = { Icon(Icons.Default.Face, contentDescription = null) },
                supportingContent = { Text("사용할 닉네임을 입력하세요") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthSlotTextField(
                value = uiState.userId,
                onValueChange = viewModel::onUserIdChanged,
                labelContent = { Text("아이디") },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
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
                onClick = viewModel::signUp,
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("가입하기")
                }
            }
        }
    }
}
