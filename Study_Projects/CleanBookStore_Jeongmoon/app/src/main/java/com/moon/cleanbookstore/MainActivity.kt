package com.moon.cleanbookstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moon.cleanbookstore.ui.MainScreen
import com.moon.cleanbookstore.ui.theme.CleanBookStore_JeongmoonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as BookStoreApplication).container

        setContent {
            CleanBookStore_JeongmoonTheme {
                MainScreen(viewModelFactory = appContainer.viewModelFactory)
            }
        }
    }
}