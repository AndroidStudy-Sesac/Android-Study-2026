package com.subin.cleanbookstore.presentation.bookmark

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.presentation.components.BookItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    viewModel: BookmarkViewModel = hiltViewModel(),
    onBookClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("내 즐겨찾기") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is BookmarkUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is BookmarkUiState.Success -> {
                    if (state.bookmarks.isEmpty()) {
                        EmptyBookmarkContent(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = state.bookmarks,
                                key = { it.id }
                            ) { book ->
                                BookItem(
                                    book = book,
                                    isLiked = true,
                                    onClick = { onBookClick(book.id) },
                                    onLikeClick = { viewModel.onBookmarkClick(book) }
                                )
                            }
                        }
                    }
                }
                is BookmarkUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkScreenContent(
    uiState: BookmarkUiState,
    onBookClick: (String) -> Unit,
    onLikeClick: (Book) -> Unit
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("내 즐겨찾기") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is BookmarkUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is BookmarkUiState.Success -> {
                    if (uiState.bookmarks.isEmpty()) {
                        EmptyBookmarkContent(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn {
                            items(uiState.bookmarks) { book ->
                                BookItem(book = book, isLiked = true, onClick = { onBookClick(book.id) }, onLikeClick = { onLikeClick(book) })
                            }
                        }
                    }
                }
                is BookmarkUiState.Error -> Text(uiState.message, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun EmptyBookmarkContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "아직 저장된 도서가 없습니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "마음에 드는 책을 찾아 북마크해 보세요!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true, name = "북마크 리스트 있음")
@Composable
fun BookmarkScreenSuccessPreview() {
    val mockBookmarks = listOf(
        Book(
            id = "1",
            title = "클린 아키텍처",
            subtitle = "소프트웨어 구조와 설계의 원칙",
            authors = listOf("로버트 C. 마틴"),
            publisher = "인사이트",
            description = "",
            imageUrl = "",
            buyLink = "",
            isFavorite = true
        ),
        Book(
            id = "2",
            title = "Kotlin in Action",
            subtitle = "코틀린 핵심 원리",
            authors = listOf("드미트리 제메로프"),
            publisher = "에이콘",
            description = "",
            imageUrl = "",
            buyLink = "",
            isFavorite = true
        )
    )

    MaterialTheme {
        Surface {
            BookmarkScreenContent(
                uiState = BookmarkUiState.Success(mockBookmarks),
                onBookClick = {},
                onLikeClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "북마크 비어있음")
@Composable
fun BookmarkScreenEmptyPreview() {
    MaterialTheme {
        Surface {
            BookmarkScreenContent(
                uiState = BookmarkUiState.Success(emptyList()),
                onBookClick = {},
                onLikeClick = {}
            )
        }
    }
}
