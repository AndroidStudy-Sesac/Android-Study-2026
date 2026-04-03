package com.jeong.cleanbookstore.screen.main.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
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
fun SearchScreen(
    viewModel: SearchTabViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(),
    onBookClick: (BookModel) -> Unit = {},
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        if (state is SearchTabState.Uninitialized) {
            viewModel.fetchData()
        }
    }

    SearchContent(
        state = state,
        searchQuery = searchQuery,
        paddingValues = paddingValues,
        onQueryChange = { searchQuery = it },
        onSearch = {
            viewModel.searchByKeyword(searchQuery)
            keyboardController?.hide()
        },
        onBookClick = onBookClick,
        onLikeClick = { book -> viewModel.toggleBookmark(book) },
    )
}

@Composable
private fun SearchContent(
    state: SearchTabState,
    searchQuery: String,
    paddingValues: PaddingValues,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBookClick: (BookModel) -> Unit,
    onLikeClick: (BookModel) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            label = { Text(text = "Search Books") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions =
                KeyboardActions(
                    onSearch = { onSearch() },
                ),
        )

        when (state) {
            is SearchTabState.Uninitialized -> {
                EmptyContent(
                    message = "검색어를 입력해 주세요.",
                )
            }

            is SearchTabState.Loading -> {
                LoadingContent()
            }

            is SearchTabState.Success -> {
                if (state.books.isEmpty()) {
                    EmptyContent(
                        message =
                            if (state.searchKeyword.isBlank()) {
                                "검색 결과가 없습니다."
                            } else {
                                "\"${state.searchKeyword}\"에 대한 검색 결과가 없습니다."
                            },
                    )
                } else {
                    SearchResultList(
                        books = state.books,
                        onBookClick = onBookClick,
                        onLikeClick = onLikeClick,
                    )
                }
            }

            is SearchTabState.Error -> {
                ErrorContent(
                    message = state.message,
                )
            }
        }
    }
}

@Composable
private fun SearchResultList(
    books: List<BookModel>,
    onBookClick: (BookModel) -> Unit,
    onLikeClick: (BookModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = books,
            key = { it.id },
        ) { book ->
            BookItem(
                book = book,
                onClick = { onBookClick(book) },
                onLikeClick = { onLikeClick(book) },
            )
        }
    }
}

@Preview(showBackground = true, name = "Success State")
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Success State - Dark",
)
@Composable
private fun SearchScreenSuccessPreview() {
    CleanBookstoreTheme {
        SearchContent(
            state =
                SearchTabState.Success(
                    books =
                        listOf(
                            BookModel(
                                id = "1",
                                title = "Jetpack Compose",
                                subtitle = "Modern Android UI",
                                authors = listOf("Google"),
                                publisher = "Google Press",
                                publishedDate = "2024",
                                description = "Description",
                                thumbnail = null,
                                previewLink = null,
                                infoLink = null,
                                isLiked = false,
                            ),
                            BookModel(
                                id = "2",
                                title = "Kotlin Coroutines",
                                subtitle = "Asynchronous Programming",
                                authors = listOf("JetBrains"),
                                publisher = "JetBrains",
                                publishedDate = "2023",
                                description = "Description",
                                thumbnail = null,
                                previewLink = null,
                                infoLink = null,
                                isLiked = true,
                            ),
                        ),
                    searchKeyword = "Compose",
                ),
            searchQuery = "Compose",
            paddingValues = PaddingValues(),
            onQueryChange = {},
            onSearch = {},
            onBookClick = {},
            onLikeClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun SearchScreenLoadingPreview() {
    CleanBookstoreTheme {
        SearchContent(
            state = SearchTabState.Loading,
            searchQuery = "",
            paddingValues = PaddingValues(),
            onQueryChange = {},
            onSearch = {},
            onBookClick = {},
            onLikeClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun SearchScreenErrorPreview() {
    CleanBookstoreTheme {
        SearchContent(
            state = SearchTabState.Error(message = "An error occurred while searching."),
            searchQuery = "Compose",
            paddingValues = PaddingValues(),
            onQueryChange = {},
            onSearch = {},
            onBookClick = {},
            onLikeClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty Result State")
@Composable
private fun SearchScreenEmptyPreview() {
    CleanBookstoreTheme {
        SearchContent(
            state = SearchTabState.Success(books = emptyList(), searchKeyword = "Nonexistent Book"),
            searchQuery = "Nonexistent Book",
            paddingValues = PaddingValues(),
            onQueryChange = {},
            onSearch = {},
            onBookClick = {},
            onLikeClick = {},
        )
    }
}
