package com.jeong.cleanbookstore.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jeong.cleanbookstore.ui.theme.CleanBookstoreTheme

@Composable
fun LoadingContent(innerPadding: PaddingValues = PaddingValues()) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingContentPreview() {
    CleanBookstoreTheme {
        LoadingContent()
    }
}
