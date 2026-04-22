package com.moon.cleanbookstore.feature.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.moon.cleanbookstore.feature.di.AppViewModelFactory
import com.moon.cleanbookstore.feature.search.SearchScreen
import com.moon.cleanbookstore.feature.search.SearchTabViewModel

fun NavGraphBuilder.bookSearchGraph(
    viewModelFactory: AppViewModelFactory,
    onBookClick: (String, String) -> Unit,
    paddingValues: PaddingValues,
    listState: LazyListState
) {
    composable<Route.Search> {
        val viewModel: SearchTabViewModel = viewModel(factory = viewModelFactory)

        SearchScreen(
            viewModel = viewModel,
            paddingValues = paddingValues,
            onBookClick = onBookClick,
            listState = listState
        )
    }
}