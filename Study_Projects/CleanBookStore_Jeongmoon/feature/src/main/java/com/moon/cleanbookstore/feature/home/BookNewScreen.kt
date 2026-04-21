package com.moon.cleanbookstore.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
fun BookNewScreen(
    viewModel: BookNewTabViewModel,
    paddingValues: PaddingValues,
    onBookClick: (String, String) -> Unit,
    listState: LazyListState
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state is NewTabState.Uninitialized) {
            viewModel.fetchData()
        }
    }

    BookNewContent(
        state = state,
        paddingValues = paddingValues,
        onBookClick = onBookClick,
        onLikeClick = { viewModel.toggleBookmark(it) },
        listState = listState
    )
}

@Composable
fun BookNewContent(
    state: NewTabState,
    paddingValues: PaddingValues,
    onBookClick: (String, String) -> Unit,
    onLikeClick: (Book) -> Unit,
    listState: LazyListState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        when (val currentState = state) {
            is NewTabState.Uninitialized, is NewTabState.Loading -> {
                CircularProgressIndicator()
            }
            is NewTabState.Error -> {
                Text(text = "에러: ${currentState.message}", color = MaterialTheme.colorScheme.error)
            }
            is NewTabState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    items(currentState.books, key = { it.id }) { book ->
                        BookItem(
                            book = book,
                            onClick = { onBookClick(book.id, book.title) },
                            onLikeClick = { onLikeClick(book) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookNewContentPreview() {
    MaterialTheme {
        Surface {
            BookNewContent(
                state = NewTabState.Success(
                    books = listOf(
                        Book("1", "클린 아키텍처", "소프트웨어 구조", listOf("로버트 마틴"), "", "", "", false),
                        Book("2", "리팩토링", "코드 구조 개선", listOf("마틴 파울러"), "", "", "", true)
                    )
                ),
                paddingValues = PaddingValues(),
                onBookClick = { _, _ -> },
                onLikeClick = {},
                listState = rememberLazyListState()
            )
        }
    }
}