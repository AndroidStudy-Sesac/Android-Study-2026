package com.subin.cleanbookstore.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.subin.cleanbookstore.domain.model.Book
import com.subin.cleanbookstore.presentation.components.BookItem

@Composable
fun SearchScreen(
    viewModel: BookSearchViewModel,
    onBookClick: (String) -> Unit
) {
    val searchPagingItems = viewModel.searchPagingData.collectAsLazyPagingItems()
    val recentHistory by viewModel.recentSearchHistory.collectAsStateWithLifecycle()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchContent(
        books = searchPagingItems,
        recentHistory = recentHistory,
        searchQuery = searchQuery,
        onQueryChange = { query ->
            searchQuery = query
            if (query.isEmpty()) {
                viewModel.resetSearchState()
            }
        },
        onClearQuery = {
            searchQuery = ""
            viewModel.resetSearchState()
        },
        onSearch = {
            if (searchQuery.isNotBlank()) {
                viewModel.searchBooks(searchQuery)
                keyboardController?.hide()
            }
        },
        onBookClick = onBookClick,
        onLikeClick = { book -> viewModel.onBookmarkClick(book) },
        onHistoryClick = { history ->
            searchQuery = history
            viewModel.searchBooks(history)
            keyboardController?.hide()
        },
        onDeleteHistory = { history -> viewModel.deleteHistory(history) },
        onClearAllHistory = { viewModel.clearAllHistory() }
    )
}

@Composable
private fun SearchContent(
    books: LazyPagingItems<Book>,
    recentHistory: List<String>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onBookClick: (String) -> Unit,
    onLikeClick: (Book) -> Unit,
    onHistoryClick: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearAllHistory: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("도서 검색") },
            placeholder = { Text("제목을 입력해 보세요") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "지우기")
                    }
                } else {
                    IconButton(onClick = onSearch) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "검색 아이콘")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (searchQuery.isEmpty()) {
                RecentSearchList(
                    history = recentHistory,
                    onHistoryClick = onHistoryClick,
                    onDeleteHistory = onDeleteHistory,
                    onClearAllHistory = onClearAllHistory
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        count = books.itemCount
                    ) { index ->
                        val book = books[index]
                        if (book != null) {
                            BookItem(
                                book = book,
                                isLiked = book.isFavorite,
                                onClick = { onBookClick(book.id) },
                                onLikeClick = { onLikeClick(book) }
                            )
                        }
                    }

                    books.apply {
                        when {
                            loadState.refresh is LoadState.Loading -> {
                                item {
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            loadState.append is LoadState.Loading -> {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            loadState.refresh is LoadState.Error -> {
                                val error = books.loadState.refresh as LoadState.Error
                                item {
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = error.error.localizedMessage ?: "에러가 발생했습니다.",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSearchList(
    history: List<String>,
    onHistoryClick: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearAllHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 검색어",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearAllHistory) {
                    Text("전체 삭제", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("최근 검색 기록이 없습니다.", color = Color.LightGray)
            }
        } else {
            LazyColumn {
                items(history) { keyword ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onHistoryClick(keyword) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = keyword,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        IconButton(onClick = { onDeleteHistory(keyword) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "삭제",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
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
fun RecentSearchListPreview() {
    MaterialTheme {
        RecentSearchList(
            history = listOf("안드로이드", "Kotlin", "Clean Architecture"),
            onHistoryClick = {},
            onDeleteHistory = {},
            onClearAllHistory = {}
        )
    }
}