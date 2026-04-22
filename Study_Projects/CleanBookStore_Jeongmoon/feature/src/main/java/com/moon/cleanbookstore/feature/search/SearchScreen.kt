package com.moon.cleanbookstore.feature.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.shared.widget.BookItem
import com.moon.cleanbookstore.shared.widget.SearchHistoryItem

@Composable
fun SearchScreen(
    viewModel: SearchTabViewModel,
    paddingValues: PaddingValues,
    onBookClick: (String, String) -> Unit,
    listState: LazyListState
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    SearchContent(
        state = state,
        paddingValues = paddingValues,
        onBookClick = onBookClick,
        onSearch = { viewModel.searchBooks(it) },
        onRemoveHistory = { viewModel.removeHistory(it) },
        onToggleBookmark = { viewModel.toggleBookmark(it) },
        listState = listState
    )
}

@Composable
fun SearchContent(
    state: SearchTabState,
    paddingValues: PaddingValues,
    onBookClick: (String, String) -> Unit,
    onSearch: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onToggleBookmark: (Book) -> Unit,
    listState: LazyListState
) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("도서명, 저자 등을 검색해보세요") },
            trailingIcon = {
                IconButton(onClick = {
                    onSearch(searchQuery)
                    keyboardController?.hide()
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(searchQuery)
                    keyboardController?.hide()
                }
            ),
            singleLine = true
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val currentState = state) {
                is SearchTabState.Uninitialized -> { }
                is SearchTabState.Loading -> CircularProgressIndicator()
                is SearchTabState.Error -> Text(text = "에러: ${currentState.message}", color = MaterialTheme.colorScheme.error)

                is SearchTabState.History -> {
                    if (currentState.keywords.isEmpty()) {
                        Text(text = "최근 검색 기록이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(currentState.keywords) { keyword ->
                                SearchHistoryItem(
                                    keyword = keyword,
                                    onHistoryClick = {
                                        searchQuery = it
                                        onSearch(it)
                                        keyboardController?.hide()
                                    },
                                    onRemoveClick = { onRemoveHistory(it) }
                                )
                            }
                        }
                    }
                }

                is SearchTabState.Success -> {
                    if (currentState.books.isEmpty()) {
                        Text(text = "'${currentState.currentKeyword}'에 대한 결과가 없습니다.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        ) {
                            items(currentState.books, key = { it.id }) { book ->
                                BookItem(
                                    book = book,
                                    onClick = { onBookClick(book.id, book.title) },
                                    onLikeClick = { onToggleBookmark(book) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "검색 기록 화면")
@Composable
fun SearchContentHistoryPreview() {
    MaterialTheme {
        Surface {
            SearchContent(
                state = SearchTabState.History(listOf("코틀린", "안드로이드", "클린 아키텍처")),
                paddingValues = PaddingValues(),
                onBookClick = { _, _ -> },
                onSearch = {},
                onRemoveHistory = {},
                onToggleBookmark = {},
                listState = rememberLazyListState()
            )
        }
    }
}

@Preview(showBackground = true, name = "검색 결과 화면")
@Composable
fun SearchContentResultPreview() {
    MaterialTheme {
        Surface {
            SearchContent(
                state = SearchTabState.Success(
                    books = listOf(Book("1", "코틀린 인 액션", "Kotlin In Action", listOf("Dmitry Jemerov"), "", "", "", false)),
                    currentKeyword = "코틀린"
                ),
                paddingValues = PaddingValues(),
                onBookClick = { _, _ -> },
                onSearch = {},
                onRemoveHistory = {},
                onToggleBookmark = {},
                listState = rememberLazyListState()
            )
        }
    }
}