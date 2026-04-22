package com.jeong.cleanbookstore.navigation

sealed class Route(
    val route: String,
) {
    data object Search : Route("search")

    data object Bookmark : Route("bookmark")

    data object Detail : Route("detail/{bookId}/{title}") {
        fun createRoute(
            bookId: String,
            title: String,
        ): String = "detail/$bookId/$title"
    }

    fun getTabIndex(route: String?): Int =
        when (route) {
            Route.Search.route -> {
                0
            }

            Route.Bookmark.route -> {
                1
            }

            else -> {
                if (route?.startsWith("detail") == true) 99 else -1
            }
        }
}
