package com.subin.cleanbookstore.presentation.navigation

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {
    @Serializable
    data object Search : NavRoute {
        val title = "검색"
        val icon = Icons.Default.Search
    }

    @Serializable
    data object Bookmark : NavRoute {
        val title = "북마크"
        val icon = Icons.Default.Favorite
    }

    @Serializable
    data class Detail(val bookId: String) : NavRoute
}