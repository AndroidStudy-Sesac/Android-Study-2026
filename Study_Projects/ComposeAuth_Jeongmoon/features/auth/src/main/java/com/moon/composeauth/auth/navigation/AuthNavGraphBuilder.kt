package com.moon.composeauth.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.moon.composeauth.auth.screen.LoginOrSingUpScreen
import com.moon.composeauth.auth.screen.LoginRoute
import com.moon.composeauth.auth.screen.SignUpRoute
import com.moon.composeauth.auth.screen.SplashScreen
import com.moon.composeauth.auth.viewmodel.AuthViewModel

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    viewModel: AuthViewModel,
    onShowSnackbar: (String) -> Unit
) {
    composable("splash") {
        SplashScreen(onSplashFinished = {
            navController.navigate("select") {
                popUpTo("splash") { inclusive = true }
            }
        })
    }

    composable("select") {
        LoginOrSingUpScreen(
            onNavigateToLogin = { navController.navigate("login") },
            onNavigateToJoin = { navController.navigate("signup") }
        )
    }

    composable("login") {
        LoginRoute(
            viewModel = viewModel,
            isExistingMember = false,
            onBackClick = { navController.popBackStack() },
            onLoginSuccess = { nickname ->
                navController.navigate("home") {
                    popUpTo("select") { inclusive = true }
                }
            },
            onShowSnackbar = onShowSnackbar
        )
    }

    composable("signup") {
        SignUpRoute(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() },
            onSignUpSuccess = { navController.navigate("login") },
            onShowSnackbar = onShowSnackbar
        )
    }
}