package com.subin.cleanbookstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.subin.cleanbookstore.presentation.MainScreen
import com.subin.cleanbookstore.ui.theme.CleanBookStoreTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanBookStoreTheme {
                MainScreen()
            }
        }
    }
}