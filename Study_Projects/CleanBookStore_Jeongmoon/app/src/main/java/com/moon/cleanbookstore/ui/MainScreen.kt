package com.moon.cleanbookstore.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moon.cleanbookstore.feature.di.AppViewModelFactory
import com.moon.cleanbookstore.feature.navigation.*

@Composable
fun MainScreen(viewModelFactory: AppViewModelFactory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem.New,
        BottomNavItem.Search,
        BottomNavItem.Bookmark
    )

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hasRoute(item.route::class) == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
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
        }
    ) { innerPadding ->
        val homeListState = rememberLazyListState()
        val searchListState = rememberLazyListState()
        val bookmarkListState = rememberLazyListState()

        NavHost(
            navController = navController,
            startDestination = Route.New
        ) {
            bookNewGraph(
                viewModelFactory = viewModelFactory,
                onBookClick = { id, title -> navController.navigate(Route.Detail(id, title)) },
                paddingValues = innerPadding,
                listState = homeListState
            )

            bookSearchGraph(
                viewModelFactory = viewModelFactory,
                onBookClick = { id, title -> navController.navigate(Route.Detail(id, title)) },
                paddingValues = innerPadding,
                listState = searchListState
            )

            bookmarkGraph(
                viewModelFactory = viewModelFactory,
                onBookClick = { id, title -> navController.navigate(Route.Detail(id, title)) },
                paddingValues = innerPadding,
                listState = bookmarkListState
            )

            bookDetailGraph(
                viewModelFactory = viewModelFactory,
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}