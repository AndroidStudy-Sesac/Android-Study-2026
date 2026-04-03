package com.jeong.cleanbookstore.screen.detail

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jeong.cleanbookstore.model.book.BookDetailModel
import com.jeong.cleanbookstore.ui.theme.CleanBookstoreTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    viewModel: BookDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state is BookDetailState.Uninitialized) {
            viewModel.fetchData()
        }
    }

    BookDetailContent(
        state = state,
        onBackClick = onBackClick,
        onRetry = viewModel::fetchData,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailContent(
    state: BookDetailState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title =
                        when (state) {
                            is BookDetailState.Success -> state.book.title
                            else -> "Book Detail"
                        }
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            is BookDetailState.Uninitialized,
            is BookDetailState.Loading,
            -> {
                LoadingContent(innerPadding)
            }

            is BookDetailState.Success -> {
                DetailContent(
                    state = state,
                    innerPadding = innerPadding,
                )
            }

            is BookDetailState.Error -> {
                ErrorContent(
                    message = state.message,
                    innerPadding = innerPadding,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(innerPadding: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    innerPadding: PaddingValues,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

@Composable
private fun DetailContent(
    state: BookDetailState.Success,
    innerPadding: PaddingValues,
) {
    val book = state.book

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(book.thumbnail)
                        .crossfade(true)
                        .build(),
                contentDescription = book.title,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                contentScale = ContentScale.Fit,
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        if (!book.subtitle.isNullOrBlank()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (book.authors.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "저자: ${book.authors.joinToString()}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (!book.publisher.isNullOrBlank()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "출판사: ${book.publisher}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        if (!book.publishedDate.isNullOrBlank()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "출간일: ${book.publishedDate}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (book.pageCount != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "페이지 수: ${book.pageCount}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (book.categories.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "카테고리: ${book.categories.joinToString()}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        if (!book.description.isNullOrBlank()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "설명",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
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
private fun BookDetailScreenSuccessPreview() {
    val sampleBook =
        BookDetailModel(
            id = "1",
            title = "Jetpack Compose Essentials",
            subtitle = "Modern Android UI Development",
            authors = listOf("John Doe", "Jane Doe"),
            publisher = "Sample Publisher",
            publishedDate = "2025-01-01",
            description =
                "This is a comprehensive guide to building modern Android UIs using Jetpack Compose. " +
                    "It covers everything from basic components to advanced layouts and animations.",
            thumbnail = null,
            previewLink = null,
            infoLink = null,
            pageCount = 350,
            categories = listOf("Technology", "Programming"),
        )

    CleanBookstoreTheme {
        BookDetailContent(
            state = BookDetailState.Success(book = sampleBook),
            onBackClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun BookDetailScreenLoadingPreview() {
    CleanBookstoreTheme {
        BookDetailContent(
            state = BookDetailState.Loading,
            onBackClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun BookDetailScreenErrorPreview() {
    CleanBookstoreTheme {
        BookDetailContent(
            state = BookDetailState.Error(message = "Failed to load book details."),
            onBackClick = {},
            onRetry = {},
        )
    }
}
