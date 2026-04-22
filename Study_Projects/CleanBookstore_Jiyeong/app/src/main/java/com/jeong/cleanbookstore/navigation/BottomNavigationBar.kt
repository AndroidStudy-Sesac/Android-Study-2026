package com.jeong.cleanbookstore.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@Composable
fun BottomNavigationBar(
    bottomNavItems: List<BottomNavItem>,
    navController: NavHostController,
    currentDestination: NavDestination?,
    onTabSelected: (Int) -> Unit,
) {
    NavigationBar {
        bottomNavItems.forEachIndexed { index, item ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == item.route.route } == true

            NavigationTabItem(
                selected = selected,
                item = item,
                navController = navController,
                onTabSelected = { onTabSelected(index) },
            )
        }
    }
}

@Composable
private fun RowScope.NavigationTabItem(
    selected: Boolean,
    item: BottomNavItem,
    navController: NavHostController,
    onTabSelected: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = {
            if (!selected) {
                navController.navigate(item.route.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                onTabSelected()
            }
        },
        icon = {
            Icon(
                imageVector = ImageVector.vectorResource(id = item.iconResId),
                contentDescription = item.label,
                modifier = Modifier.size(24.dp),
            )
        },
        label = {
            Text(text = item.label)
        },
    )
}
