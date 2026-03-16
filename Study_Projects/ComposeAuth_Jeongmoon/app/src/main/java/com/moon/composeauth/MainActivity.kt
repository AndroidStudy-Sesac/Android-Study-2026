package com.moon.composeauth

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moon.composeauth.auth.screen.EntryPointScreen
import com.moon.composeauth.auth.viewmodel.AuthViewModel
import com.moon.composeauth.auth.viewmodel.AuthViewModelFactory
import com.moon.composeauth.data.database.AppDatabase
import com.moon.composeauth.data.impl.UserRepositoryImpl
import com.moon.composeauth.ui.theme.ComposeAuth_JeongmoonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val userRepository = UserRepositoryImpl(database.userDao())
        val factory = AuthViewModelFactory(userRepository)

        enableEdgeToEdge()
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        setContent {
            ComposeAuth_JeongmoonTheme {
                val authViewModel: AuthViewModel = viewModel(factory = factory)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EntryPointScreen(
                        viewModel = authViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}