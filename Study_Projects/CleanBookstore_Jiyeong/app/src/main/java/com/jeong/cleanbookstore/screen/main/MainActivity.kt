package com.jeong.cleanbookstore.screen.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jeong.cleanbookstore.navigation.BottomNavigationBar
import com.jeong.cleanbookstore.navigation.MainNavHost
import com.jeong.cleanbookstore.ui.theme.CleanBookstoreTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanBookstoreTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                bottomNavItems = viewModel.bottomNavItems,
                navController = navController,
                currentDestination = currentDestination,
                onTabSelected = viewModel::onTabSelected,
            )
        },
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
}
