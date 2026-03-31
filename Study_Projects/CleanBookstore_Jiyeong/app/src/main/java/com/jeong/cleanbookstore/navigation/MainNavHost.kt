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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jeong.cleanbookstore.screen.main.search.SearchScreen

@Suppress("ktlint:standard:function-naming")
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
                onLikeClick = {
                },
            )
        }
        composable(Route.Bookmark.route) {
            PlaceholderScreen(
                text = "Bookmark Screen",
                innerPadding = innerPadding,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
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
