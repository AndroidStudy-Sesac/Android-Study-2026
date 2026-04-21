package com.moon.cleanbookstore.feature.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.moon.cleanbookstore.domain.model.Book
import com.moon.cleanbookstore.domain.model.BookMemo

@Composable
fun BookDetailScreen(
    viewModel: BookDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    BookDetailContent(
        state = state,
        onBackClick = onBackClick,
        onToggleBookmark = { viewModel.toggleBookmark() },
        onSaveMemo = { viewModel.saveMemo(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailContent(
    state: BookDetailState,
    onBackClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSaveMemo: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("도서 상세") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state is BookDetailState.Success) {
                        val isBookmarked = state.book.isBookmarked
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color.Red else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val currentState = state) {
                is BookDetailState.Loading -> CircularProgressIndicator()
                is BookDetailState.Error -> Text(currentState.message)
                is BookDetailState.Success -> {
                    val book = currentState.book
                    // domain 모델의 변수명에 맞게 memo 혹은 content를 사용하세요
                    var memoText by remember { mutableStateOf(currentState.memo?.memoContent ?: "") }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        AsyncImage(
                            model = book.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = book.title, style = MaterialTheme.typography.headlineMedium)
                        Text(text = book.authors.joinToString(", "), color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = book.description)

                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "나의 메모", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = memoText,
                            onValueChange = { memoText = it },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            placeholder = { Text("이 책에 대한 생각을 남겨보세요...") }
                        )
                        Button(
                            onClick = { onSaveMemo(memoText) },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("메모 저장")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookDetailContentPreview() {
    MaterialTheme {
        Surface {
            BookDetailContent(
                state = BookDetailState.Success(
                    book = Book("1", "안드로이드 프로그래밍", "Compose 완벽 가이드", listOf("개발자"), "상세한 설명입니다.", "", "", true),
                    memo = BookMemo(
                        "1", "이 책 꼭 사야지!", System.currentTimeMillis())
                ),
                onBackClick = {},
                onToggleBookmark = {},
                onSaveMemo = {}
            )
        }
    }
}