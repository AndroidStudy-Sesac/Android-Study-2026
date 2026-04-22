package com.moon.cleanbookstore.feature.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.moon.cleanbookstore.feature.bookmark.BookmarkScreen
import com.moon.cleanbookstore.feature.bookmark.BookmarkTabViewModel
import com.moon.cleanbookstore.feature.di.AppViewModelFactory

fun NavGraphBuilder.bookmarkGraph(
    viewModelFactory: AppViewModelFactory,
    onBookClick: (String, String) -> Unit,
    paddingValues: PaddingValues,
    listState: LazyListState
) {
    composable<Route.Bookmark> {
        val viewModel: BookmarkTabViewModel = viewModel(factory = viewModelFactory)

        BookmarkScreen(
            viewModel = viewModel,
            paddingValues = paddingValues,
            onBookClick = onBookClick,
            listState = listState
        )
    }
}