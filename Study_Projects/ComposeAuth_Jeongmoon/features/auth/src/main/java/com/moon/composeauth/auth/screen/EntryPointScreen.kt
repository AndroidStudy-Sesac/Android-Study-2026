package com.moon.composeauth.auth.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moon.composeauth.auth.navigation.authNavGraph
import com.moon.composeauth.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun EntryPointScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            authNavGraph(
                navController = navController,
                viewModel = viewModel,
                onShowSnackbar = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )
            composable("home") { HomeMainScreen() }
        }
    }
}