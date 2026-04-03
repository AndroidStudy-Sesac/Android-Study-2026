package com.jeong.cleanbookstore.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jeong.cleanbookstore.screen.detail.BookDetailScreen
import com.jeong.cleanbookstore.screen.main.bookmark.BookmarkScreen
import com.jeong.cleanbookstore.screen.main.search.SearchScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Search.route,
    ) {
        composable(Route.New.route) {
            PlaceholderScreen(
                text = "New Screen",
                innerPadding = innerPadding,
            )
        }
        composable(Route.Search.route) {
            SearchScreen(
                paddingValues = innerPadding,
                onBookClick = { book ->
                    navController.navigate(
                        Route.Detail.createRoute(
                            bookId = book.id,
                            title = book.title,
                        ),
                    )
                },
            )
        }
        composable(Route.Bookmark.route) {
            BookmarkScreen(
                paddingValues = innerPadding,
                onBookClick = { book ->
                    navController.navigate(
                        Route.Detail.createRoute(
                            bookId = book.id,
                            title = book.title,
                        ),
                    )
                },
            )
        }
        composable(
            Route.Detail.route,
            arguments =
                listOf(
                    navArgument("bookId") {
                        type = NavType.StringType
                    },
                    navArgument("title") {
                        type = NavType.StringType
                    },
                ),
        ) {
            BookDetailScreen(
                paddingValues = innerPadding,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun PlaceholderScreen(
    text: String,
    innerPadding: PaddingValues,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text)
    }
}
