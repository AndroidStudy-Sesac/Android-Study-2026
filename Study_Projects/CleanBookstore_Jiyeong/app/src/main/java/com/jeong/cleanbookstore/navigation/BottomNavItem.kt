package com.jeong.cleanbookstore.navigation

import com.jeong.cleanbookstore.R

sealed class BottomNavItem(
    val route: Route,
    val iconResId: Int,
    val label: String,
) {
    data object Search : BottomNavItem(
        route = Route.Search,
        iconResId = R.drawable.outline_back_to_tab_24,
        label = "Search",
    )

    data object Bookmark : BottomNavItem(
        route = Route.Bookmark,
        iconResId = R.drawable.outline_bookmark_24,
        label = "Bookmark",
    )
}
