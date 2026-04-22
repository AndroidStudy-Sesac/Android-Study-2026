package com.jeong.cleanbookstore.screen.detail

import androidx.lifecycle.SavedStateHandle
import com.jeong.cleanbookstore.data.repository.BookSearchRepository
import com.jeong.cleanbookstore.model.book.BookModel
import com.jeong.cleanbookstore.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherRule::class)
class BookDetailViewModelTest {
    private lateinit var bookSearchRepository: BookSearchRepository
    private lateinit var viewModel: BookDetailViewModel

    @BeforeEach
    fun setUp() {
        bookSearchRepository = mockk()
    }

    @Test
    fun `초기 상태는 Uninitialized이다`() =
        runTest {
            val savedStateHandle = SavedStateHandle(mapOf("bookId" to "book-1"))
            viewModel =
                BookDetailViewModel(
                    bookSearchRepository = bookSearchRepository,
                    savedStateHandle = savedStateHandle,
                )

            val state = viewModel.stateFlow.value

            assertTrue(state is BookDetailState.Uninitialized)
        }

    @Test
    fun `fetchData 성공 시 Loading을 거쳐 Success 상태가 된다`() =
        runTest {
            // Given
            val bookId = "book-1"
            val detailModel =
                BookModel(
                    id = bookId,
                    title = "Android Internals",
                    subtitle = "Deep Dive",
                    authors = listOf("John Doe"),
                    publisher = "Sample Publisher",
                    publishedDate = "2024-01-01",
                    description = "Detail description",
                    thumbnail = "https://example.com/image.jpg",
                    previewLink = "https://example.com/preview",
                    infoLink = "https://example.com/info",
                    pageCount = 320,
                    categories = listOf("Computers"),
                )

            val savedStateHandle = SavedStateHandle(mapOf("bookId" to bookId))

            // coAnswers와 delay를 사용하여 Loading 상태가 수집될 시간을 확보함
            coEvery {
                bookSearchRepository.getBookDetail(volumeId = bookId)
            } coAnswers {
                delay(1)
                detailModel
            }

            viewModel =
                BookDetailViewModel(
                    bookSearchRepository = bookSearchRepository,
                    savedStateHandle = savedStateHandle,
                )

            // When & Then
            val states = mutableListOf<BookDetailState>()
            val collectJob =
                launch(UnconfinedTestDispatcher()) {
                    viewModel.stateFlow.collect { states.add(it) }
                }

            viewModel.fetchData().join()

            assertAll(
                {
                    assertTrue(
                        states.contains(BookDetailState.Loading),
                        "Loading 상태를 거쳐야 합니다. (수집된 상태들: $states)",
                    )
                },
                { assertTrue(states.last() is BookDetailState.Success, "마지막 상태는 Success여야 합니다.") },
                {
                    val successState = states.last() as BookDetailState.Success
                    assertEquals(bookId, successState.book.id)
                    assertEquals("Android Internals", successState.book.title)
                },
            )

            collectJob.cancel()
        }

    @Test
    fun `fetchData 실패 시 Error 상태가 된다`() =
        runTest {
            // Given
            val bookId = "book-1"
            val errorMessage = "도서 상세 정보를 불러오지 못했습니다."
            val savedStateHandle = SavedStateHandle(mapOf("bookId" to bookId))

            coEvery {
                bookSearchRepository.getBookDetail(volumeId = bookId)
            } throws IllegalStateException(errorMessage)

            viewModel =
                BookDetailViewModel(
                    bookSearchRepository = bookSearchRepository,
                    savedStateHandle = savedStateHandle,
                )

            // When
            viewModel.fetchData().join()

            // Then
            val state = viewModel.stateFlow.value
            assertTrue(state is BookDetailState.Error)
            assertEquals(errorMessage, (state as BookDetailState.Error).message)
        }

    @Test
    fun `bookId가 null이면 Error 상태가 된다`() =
        runTest {
            // Given
            val savedStateHandle = SavedStateHandle()
            viewModel =
                BookDetailViewModel(
                    bookSearchRepository = bookSearchRepository,
                    savedStateHandle = savedStateHandle,
                )

            // When
            viewModel.fetchData().join()

            // Then
            val state = viewModel.stateFlow.value
            assertTrue(state is BookDetailState.Error)
            assertEquals("도서 식별자가 없습니다.", (state as BookDetailState.Error).message)
        }

    @Test
    fun `bookId가 빈 문자열이면 Error 상태가 된다`() =
        runTest {
            // Given
            val savedStateHandle = SavedStateHandle(mapOf("bookId" to ""))
            viewModel =
                BookDetailViewModel(
                    bookSearchRepository = bookSearchRepository,
                    savedStateHandle = savedStateHandle,
                )

            // When
            viewModel.fetchData().join()

            // Then
            val state = viewModel.stateFlow.value
            assertTrue(state is BookDetailState.Error)
            assertEquals("도서 식별자가 없습니다.", (state as BookDetailState.Error).message)
        }
}
