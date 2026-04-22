package com.moon.cleanbookstore.feature.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.moon.cleanbookstore.feature.detail.BookDetailScreen
import com.moon.cleanbookstore.feature.detail.BookDetailViewModel
import com.moon.cleanbookstore.feature.di.AppViewModelFactory

fun NavGraphBuilder.bookDetailGraph(
    viewModelFactory: AppViewModelFactory,
    onBackClick: () -> Unit
) {
    composable<Route.Detail> { backStackEntry ->
        val detailRoute = backStackEntry.toRoute<Route.Detail>()

        val viewModel: BookDetailViewModel = viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return viewModelFactory.createDetailViewModel(detailRoute.id) as T
                }
            }
        )

        BookDetailScreen(
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}