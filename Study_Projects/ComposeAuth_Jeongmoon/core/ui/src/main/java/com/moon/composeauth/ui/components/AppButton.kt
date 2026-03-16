package com.moon.composeauth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moon.composeauth.ui.theme.Green
import com.moon.composeauth.ui.theme.White

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontWeight: FontWeight? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Green,
            contentColor = White
        )
    ) {
        Text(text = text, fontWeight = fontWeight)
    }
}

@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            color = Green
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AppButtonPreview() {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppButton(
            text = "로그인 (기본)",
            onClick = {}
        )

        AppButton(
            text = "회원가입 (굵은 글씨)",
            onClick = {},
            fontWeight = FontWeight.Bold
        )

        AppButton(
            text = "입력 대기중 (비활성화)",
            onClick = {},
            enabled = false
        )

        AppOutlinedButton(
            text = "계정이 없으신가요?",
            onClick = {}
        )

        AppOutlinedButton(
            text = "클릭 불가 (비활성화)",
            onClick = {},
            enabled = false
        )
    }
}