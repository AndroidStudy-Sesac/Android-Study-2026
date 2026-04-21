package com.moon.cleanbookstore.feature.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.moon.cleanbookstore.feature.di.AppViewModelFactory
import com.moon.cleanbookstore.feature.home.BookNewScreen
import com.moon.cleanbookstore.feature.home.BookNewTabViewModel

fun NavGraphBuilder.bookNewGraph(
    viewModelFactory: AppViewModelFactory,
    onBookClick: (String, String) -> Unit,
    paddingValues: PaddingValues,
    listState: LazyListState
) {
    composable<Route.New> {

        val viewModel: BookNewTabViewModel = viewModel(factory = viewModelFactory)

        BookNewScreen(
            viewModel = viewModel,
            paddingValues = paddingValues,
            onBookClick = onBookClick,
            listState = listState
        )
    }
}