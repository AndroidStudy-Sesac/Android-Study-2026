package com.moon.cleanbookstore.feature.bookmark

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.shared.widget.BookItem

@Composable
fun BookmarkScreen(
    viewModel: BookmarkTabViewModel,
    paddingValues: PaddingValues,
    onBookClick: (String, String) -> Unit,
    listState: LazyListState
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
        onRemoveBookmark = { viewModel.removeBookmark(it) },
        listState = listState
    )
}

@Composable
fun BookmarkContent(
    state: BookmarkState,
    paddingValues: PaddingValues,
    onBookClick: (String, String) -> Unit,
    onRemoveBookmark: (Book) -> Unit,
    listState: LazyListState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        when (val currentState = state) {
            is BookmarkState.Uninitialized, is BookmarkState.Loading -> {
                CircularProgressIndicator()
            }
            is BookmarkState.Error -> {
                Text(text = "에러: ${currentState.message}", color = MaterialTheme.colorScheme.error)
            }
            is BookmarkState.Success -> {
                if (currentState.books.isEmpty()) {
                    Text(
                        text = "찜한 도서가 없습니다.\n관심 있는 책을 추가해보세요!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        items(currentState.books, key = { it.id }) { book ->
                            BookItem(
                                book = book,
                                onClick = { onBookClick(book.id, book.title) },
                                onLikeClick = { onRemoveBookmark(book) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkContentPreview() {
    MaterialTheme {
        Surface {
            BookmarkContent(
                state = BookmarkState.Success(
                    books = listOf(
                        Book("1", "클린 아키텍처", "소프트웨어 구조", listOf("로버트 마틴"), "", "", "", true)
                    )
                ),
                paddingValues = PaddingValues(),
                onBookClick = { _, _ -> },
                onRemoveBookmark = {},
                listState = rememberLazyListState()
            )
        }
    }
}