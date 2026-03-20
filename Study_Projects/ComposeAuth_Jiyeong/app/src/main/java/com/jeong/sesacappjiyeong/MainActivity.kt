package com.jeong.sesacappjiyeong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jeong.sesacappjiyeong.data.AppDatabase
import com.jeong.sesacappjiyeong.data.AuthRepositoryImpl
import com.jeong.sesacappjiyeong.feature.auth.ui.login.LoginScreen
import com.jeong.sesacappjiyeong.feature.auth.ui.login.LoginViewModel
import com.jeong.sesacappjiyeong.feature.auth.ui.signup.SignUpScreen
import com.jeong.sesacappjiyeong.feature.auth.ui.signup.SignUpViewModel
import com.jeong.sesacappjiyeong.ui.main.MainScreen
import com.jeong.sesacappjiyeong.ui.splash.SplashScreen
import com.jeong.sesacappjiyeong.ui.theme.SeSACAppJiyeongTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Manual Dependency Injection (Singleton)
        val database = AppDatabase.getInstance(this)
        val repository = AuthRepositoryImpl(database.userDao())

        setContent {
            SeSACAppJiyeongTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(onTimeout = {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }

                    composable("login") {
                        val loginViewModel = remember { LoginViewModel(repository) }
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { nickname ->
                                navController.navigate("main/$nickname") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToSignUp = {
                                navController.navigate("signup")
                            }
                        )
                    }

                    composable("signup") {
                        val signUpViewModel = remember { SignUpViewModel(repository) }
                        SignUpScreen(
                            viewModel = signUpViewModel,
                            onSignUpSuccess = {
                                navController.popBackStack()
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "main/{nickname}",
                        arguments = listOf(navArgument("nickname") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
                        MainScreen(nickname = nickname)
                    }
                }
            }
        }
    }
}
