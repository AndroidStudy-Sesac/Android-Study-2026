package com.subin.cleanbookstore.presentation.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.subin.cleanbookstore.domain.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    bookId: String,
    onBackClick: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(bookId) {
        viewModel.loadBookDetail(bookId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "도서 상세 정보",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    if (uiState is DetailUiState.Success) {
                        val book = (uiState as DetailUiState.Success).book
                        IconButton(onClick = { viewModel.toggleFavorite(book) }) {
                            Icon(
                                imageVector = if (book.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "좋아요",
                                tint = if (book.isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DetailUiState.Success -> {
                DetailContent(
                    modifier = Modifier.padding(padding),
                    book = state.book,
                    onMemoSave = { newMemo -> viewModel.updateMemo(bookId, newMemo) }
                )
            }
            is DetailUiState.Error -> {
                Text(text = state.message, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun DetailContent(
    modifier: Modifier,
    book: Book,
    onMemoSave: (String) -> Unit
) {
    var memoText by rememberSaveable { mutableStateOf(book.memo) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = book.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = book.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = book.authors.joinToString(", "), style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        if (!book.pdfDownloadLink.isNullOrEmpty()) {
            Button(onClick = { /* 링크 연결 로직 */ }) {
                Text("PDF 다운로드")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = memoText,
            onValueChange = { memoText = it },
            label = { Text("나만의 메모") },
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )

        Button(
            onClick = {
                if (book.isFavorite) {
                    onMemoSave(memoText)
                    Toast.makeText(context, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "상단의 하트(좋아요)를 눌러야 메모를 저장할 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text("메모 저장")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "도서 소개", style = MaterialTheme.typography.titleMedium)
        Text(text = book.description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun DetailContentPreview() {
    val dummyBook = Book(
        id = "1",
        title = "Clean Architecture 실전 가이드",
        subtitle = "안드로이드 개발자를 위한 클린 아키텍처",
        authors = listOf("로버트 C. 마틴", "수빈"),
        publisher = "수빈 출판사",
        description = "이 책은 안드로이드 주니어 개발자가 클린 아키텍처를 배우며 성장하는 과정을 담고 있습니다. UseCase부터 Mapper까지 실전 예제를 다룹니다.",
        imageUrl = "",
        buyLink = "",
        isFavorite = true,
        pdfDownloadLink = "https://example.com/sample.pdf",
        memo = "이 책 꼭 끝까지 읽기!"
    )

    MaterialTheme {
        DetailContent(
            modifier = Modifier,
            book = dummyBook,
            onMemoSave = {}
        )
    }
}