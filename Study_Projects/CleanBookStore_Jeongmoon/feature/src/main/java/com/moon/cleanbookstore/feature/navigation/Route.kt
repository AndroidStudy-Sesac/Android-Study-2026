package com.moon.cleanbookstore.feature.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object New : Route
    @Serializable
    data object Search : Route
    @Serializable
    data object Bookmark : Route

    @Serializable
    data class Detail(val id: String, val title: String) : Route
}

sealed class BottomNavItem(val route: Route, val icon: ImageVector, val label: String) {
    data object New : BottomNavItem(Route.New, Icons.Default.Home, "New")
    data object Search : BottomNavItem(Route.Search, Icons.Default.Search, "Search")
    data object Bookmark : BottomNavItem(Route.Bookmark, Icons.Default.Bookmark, "Bookmark")
}