package com.subin.composeauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subin.composeauth.data.local.AppDatabase
import com.subin.composeauth.data.repository.UserRepositoryImpl
import com.subin.composeauth.domain.usecase.LoginUseCase
import com.subin.composeauth.domain.usecase.SignUpUseCase
import com.subin.composeauth.presentation.login.LoginScreen
import com.subin.composeauth.presentation.login.LoginViewModel
import com.subin.composeauth.presentation.main.EntryPointScreen
import com.subin.composeauth.presentation.main.MainScreen
import com.subin.composeauth.presentation.signup.SignUpScreen
import com.subin.composeauth.presentation.signup.SignUpViewModel
import com.subin.composeauth.ui.theme.ComposeAuth_SubinTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            ComposeAuth_SubinTheme {
                var isCheckingDB by remember { mutableStateOf(true) }
                var startDestination by remember { mutableStateOf("entryPoint") }

                val context = LocalContext.current
                val repository = remember {
                    val database = AppDatabase.getDatabase(context)
                    UserRepositoryImpl(database.userDao())
                }

                LaunchedEffect(Unit) {
                    val autoLoginUser = repository.checkAutoLogin()

                    if (autoLoginUser != null) {
                        startDestination = "main/${autoLoginUser.nickname}"
                    } else {
                        startDestination = "entryPoint"
                    }

                    delay(500)
                    isCheckingDB = false
                }

                splashScreen.setKeepOnScreenCondition {
                    isCheckingDB
                }

                if (!isCheckingDB) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val loginUseCase = remember { LoginUseCase(repository) }
                        val signUpUseCase = remember { SignUpUseCase(repository) }
                        val navController = rememberNavController()

                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {

                            composable("entryPoint") {
                                EntryPointScreen(
                                    onNavigateToLogin = { navController.navigate("login") },
                                    onNavigateToSignUp = { navController.navigate("signup") }
                                )
                            }

                            composable("login") {
                                val viewModel: LoginViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return LoginViewModel(loginUseCase) as T
                                        }
                                    }
                                )

                                LoginScreen(
                                    viewModel = viewModel,
                                    onNavigateToMain = { nickname ->
                                        navController.navigate("main/$nickname") {
                                            popUpTo("entryPoint") { inclusive = true }
                                        }
                                    },
                                    onNavigateToSignUp = { navController.navigate("signup") }
                                )
                            }

                            composable("signup") {
                                val viewModel: SignUpViewModel = viewModel(
                                    factory = object : ViewModelProvider.Factory {
                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                            return SignUpViewModel(signUpUseCase) as T
                                        }
                                    }
                                )

                                SignUpScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(
                                route = "main/{nickname}",
                                arguments = listOf(navArgument("nickname") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val nickname =
                                    backStackEntry.arguments?.getString("nickname") ?: "Unknown"
                                MainScreen(nickname = nickname)
                            }
                        }
                    }
                }
            }
        }
    }
}
