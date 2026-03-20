package com.subin.composeauth.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subin.composeauth.ui.components.CustomButton
import com.subin.composeauth.ui.theme.ComposeAuth_SubinTheme

@Composable
fun EntryPointScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "서비스에 오신 것을 환영합니다!",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        CustomButton(onClick = onNavigateToLogin) {
            Text("로그인하기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(onClick = onNavigateToSignUp) {
            Text("회원가입")
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun EntryPointScreenPreview() {
    ComposeAuth_SubinTheme {
        EntryPointScreen(
            onNavigateToLogin = {},
            onNavigateToSignUp = {}
        )
    }
}