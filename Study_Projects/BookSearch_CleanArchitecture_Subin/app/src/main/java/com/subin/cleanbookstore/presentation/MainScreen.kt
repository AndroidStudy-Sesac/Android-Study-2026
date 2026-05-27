package com.subin.cleanbookstore.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.subin.cleanbookstore.presentation.navigation.MainNavHost
import com.subin.cleanbookstore.presentation.navigation.NavRoute

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navigationItems = listOf(
        NavRoute.Search,
        NavRoute.Bookmark
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { route ->
                    val isSelected = currentDestination?.hasRoute(route::class) ?: false

                    NavigationBarItem(
                        icon = {
                            val icon = if (route is NavRoute.Search) route.icon else (route as NavRoute.Bookmark).icon
                            Icon(icon, contentDescription = null)
                        },
                        label = {
                            val title = if (route is NavRoute.Search) route.title else (route as NavRoute.Bookmark).title
                            Text(title)
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}