package com.jeong.cleanbookstore.screen.main.bookmark

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeong.cleanbookstore.model.book.BookModel
import com.jeong.cleanbookstore.ui.component.EmptyContent
import com.jeong.cleanbookstore.ui.component.ErrorContent
import com.jeong.cleanbookstore.ui.component.LoadingContent
import com.jeong.cleanbookstore.ui.theme.CleanBookstoreTheme
import com.jeong.cleanbookstore.widget.item.BookItem

@Composable
fun BookmarkScreen(
    paddingValues: PaddingValues,
    viewModel: BookmarkViewModel = hiltViewModel(),
    onBookClick: (BookModel) -> Unit,
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        if (state is BookmarkState.Uninitialized) {
            viewModel.fetchData()
        }
    }
    BookmarkContent(
        state = state,
        paddingValues = paddingValues,
        onBookClick = onBookClick,
        onLikeClick = { book -> viewModel.removeBookmark(book.id) },
        onRetry = viewModel::fetchData,
    )
}

@Composable
private fun BookmarkContent(
    state: BookmarkState,
    paddingValues: PaddingValues,
    onBookClick: (BookModel) -> Unit,
    onLikeClick: (BookModel) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        is BookmarkState.Uninitialized,
        is BookmarkState.Loading,
        -> {
            LoadingContent(paddingValues)
        }

        is BookmarkState.Empty -> {
            EmptyContent(
                message = "저장된 북마크가 없습니다.",
                innerPadding = paddingValues,
            )
        }

        is BookmarkState.Success -> {
            BookmarkList(
                books = state.books,
                paddingValues = paddingValues,
                onBookClick = onBookClick,
                onLikeClick = onLikeClick,
            )
        }

        is BookmarkState.Error -> {
            ErrorContent(
                message = state.message,
                innerPadding = paddingValues,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun BookmarkList(
    books: List<BookModel>,
    paddingValues: PaddingValues,
    onBookClick: (BookModel) -> Unit,
    onLikeClick: (BookModel) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    LazyColumn(
        modifier = Modifier,
        contentPadding =
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top =
                    paddingValues.calculateTopPadding() + 8.dp,
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom =
                    paddingValues.calculateBottomPadding() + 16.dp,
            ),
    ) {
        items(
            items = books,
            key = { it.id },
        ) { book ->
            BookItem(
                book = book,
                onClick = onBookClick,
                onLikeClick = onLikeClick,
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun BookmarkScreenPreview() {
    val sampleBooks =
        listOf(
            BookModel(
                id = "1",
                title = "Jetpack Compose Essentials",
                subtitle = "Modern Android UI Development",
                authors = listOf("John Doe", "Jane Doe"),
                publisher = "Sample Publisher",
                publishedDate = "2025-01-01",
                description = "Sample description",
                thumbnail = null,
                previewLink = null,
                infoLink = null,
                isLiked = true,
            ),
            BookModel(
                id = "2",
                title = "Kotlin Coroutines",
                subtitle = "Deep Dive",
                authors = listOf("Marcin Moskala"),
                publisher = "Kt. Academy",
                publishedDate = "2022-01-01",
                description = "Deep dive into Kotlin Coroutines",
                thumbnail = null,
                previewLink = null,
                infoLink = null,
                isLiked = true,
            ),
        )

    CleanBookstoreTheme {
        BookmarkContent(
            state = BookmarkState.Success(sampleBooks),
            paddingValues = PaddingValues(0.dp),
            onBookClick = {},
            onLikeClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookmarkScreenEmptyPreview() {
    CleanBookstoreTheme {
        BookmarkContent(
            state = BookmarkState.Empty,
            paddingValues = PaddingValues(0.dp),
            onBookClick = {},
            onLikeClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookmarkScreenErrorPreview() {
    CleanBookstoreTheme {
        BookmarkContent(
            state = BookmarkState.Error("데이터를 불러오는 중 오류가 발생했습니다."),
            paddingValues = PaddingValues(0.dp),
            onBookClick = {},
            onLikeClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookmarkScreenLoadingPreview() {
    CleanBookstoreTheme {
        BookmarkContent(
            state = BookmarkState.Loading,
            paddingValues = PaddingValues(0.dp),
            onBookClick = {},
            onLikeClick = {},
            onRetry = {},
        )
    }
}
