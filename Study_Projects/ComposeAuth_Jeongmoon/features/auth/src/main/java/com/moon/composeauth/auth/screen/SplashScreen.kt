package com.moon.composeauth.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moon.composeauth.auth.R
import com.moon.composeauth.ui.theme.Green
import com.moon.composeauth.ui.theme.Orange
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onSplashFinished()
    }

    val gradiantBackground = Brush.horizontalGradient(
        0.0f to Green.copy(alpha = 0.7f),
        1.0f to Orange,
        startX = 0.0f,
        endX = 1000.0f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradiantBackground)
    ) {
        Image(
            modifier = Modifier
                .padding(top = 112.dp)
                .sizeIn(250.dp, 250.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop,
            painter = painterResource(R.drawable.sesac_logo),
            contentDescription = null
        )
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 200.dp),
            text = stringResource(R.string.splash_message),
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onSplashFinished = {})
}