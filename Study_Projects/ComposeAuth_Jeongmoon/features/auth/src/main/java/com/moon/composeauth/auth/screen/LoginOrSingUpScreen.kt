package com.moon.composeauth.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moon.composeauth.auth.R
import com.moon.composeauth.ui.components.AppButton
import com.moon.composeauth.ui.components.AppOutlinedButton

@Composable
fun LoginOrSingUpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToJoin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.sesac_logo),
                contentDescription = null
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppButton(
                text = stringResource(R.string.login),
                onClick = onNavigateToLogin,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AppOutlinedButton(
                text = stringResource(R.string.no_account_yet),
                onClick = onNavigateToJoin,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginOrSingUpScreenPreview() {
    LoginOrSingUpScreen(
        onNavigateToLogin = {},
        onNavigateToJoin = {}
    )
}