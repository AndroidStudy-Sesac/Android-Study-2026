package com.jeong.cleanbookstore.screen.main.search

import com.jeong.cleanbookstore.data.repository.BookSearchRepository
import com.jeong.cleanbookstore.data.repository.BookmarkRepository
import com.jeong.cleanbookstore.model.book.BookModel
import com.jeong.cleanbookstore.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherRule::class)
class SearchTabViewModelTest {
    private lateinit var bookSearchRepository: BookSearchRepository
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var bookmarkFlow: MutableStateFlow<List<BookModel>>
    private lateinit var viewModel: SearchTabViewModel

    @BeforeEach
    fun setUp() {
        bookSearchRepository = mockk()
        bookmarkRepository = mockk()
        bookmarkFlow = MutableStateFlow(emptyList())

        every { bookmarkRepository.getBookmarks() } returns bookmarkFlow
    }

    @Test
    fun `초기 상태는 Uninitialized이다`() =
        runTest {
            viewModel =
                SearchTabViewModel(
                    bookSearchRepository = bookSearchRepository,
                    bookmarkRepository = bookmarkRepository,
                )

            val state = viewModel.stateFlow.value

            assertTrue(state is SearchTabState.Uninitialized)
        }

    @Test
    fun `fetchData를 호출하면 빈 Success 상태가 된다`() =
        runTest {
            viewModel =
                SearchTabViewModel(
                    bookSearchRepository = bookSearchRepository,
                    bookmarkRepository = bookmarkRepository,
                )

            viewModel.fetchData().join()

            val state = viewModel.stateFlow.value

            assertTrue(state is SearchTabState.Success)

            val successState = state as SearchTabState.Success
            assertTrue(successState.books.isEmpty())
            assertEquals("", successState.searchKeyword)
        }

    @Test
    fun `검색 성공 시 Loading을 거쳐 북마크 상태가 합성된 Success 상태가 된다`() =
        runTest {
            val rawBooks =
                listOf(
                    BookModel(
                        id = "book-1",
                        title = "Android Clean Architecture",
                        subtitle = "Guide",
                        authors = listOf("John Doe"),
                        publisher = "Sample Publisher",
                        publishedDate = "2024-01-01",
                        description = "description",
                        thumbnail = "https://example.com/image.jpg",
                        previewLink = "https://example.com/preview",
                        infoLink = "https://example.com/info",
                        isLiked = null,
                    ),
                    BookModel(
                        id = "book-2",
                        title = "Kotlin Coroutines",
                        subtitle = "Deep Dive",
                        authors = listOf("Jane Doe"),
                        publisher = "Sample Publisher",
                        publishedDate = "2024-02-01",
                        description = "description",
                        thumbnail = "https://example.com/image2.jpg",
                        previewLink = "https://example.com/preview2",
                        infoLink = "https://example.com/info2",
                        isLiked = null,
                    ),
                )

            bookmarkFlow.value =
                listOf(
                    BookModel(
                        id = "book-1",
                        title = "Android Clean Architecture",
                        subtitle = "Guide",
                        authors = listOf("John Doe"),
                        publisher = "Sample Publisher",
                        publishedDate = "2024-01-01",
                        description = "description",
                        thumbnail = "https://example.com/image.jpg",
                        previewLink = "https://example.com/preview",
                        infoLink = "https://example.com/info",
                        isLiked = true,
                    ),
                )

            coEvery {
                bookSearchRepository.searchBooksByKeyword(
                    query = "android",
                    startIndex = 0,
                    maxResults = 20,
                )
            } coAnswers {
                delay(1)
                rawBooks
            }

            viewModel =
                SearchTabViewModel(
                    bookSearchRepository = bookSearchRepository,
                    bookmarkRepository = bookmarkRepository,
                )

            val states = mutableListOf<SearchTabState>()
            val collectJob =
                launch(UnconfinedTestDispatcher()) {
                    viewModel.stateFlow.collect { states.add(it) }
                }

            viewModel.searchByKeyword("android")
            advanceUntilIdle()

            assertAll(
                {
                    assertTrue(
                        states.contains(SearchTabState.Loading),
                        "Loading 상태를 거쳐야 합니다. (수집된 상태들: $states)",
                    )
                },
                {
                    assertTrue(
                        states.last() is SearchTabState.Success,
                        "마지막 상태는 Success여야 합니다.",
                    )
                },
                {
                    val successState = states.last() as SearchTabState.Success
                    assertEquals("android", successState.searchKeyword)
                    assertEquals(2, successState.books.size)
                    assertEquals(true, successState.books.first { it.id == "book-1" }.isLiked)
                    assertEquals(false, successState.books.first { it.id == "book-2" }.isLiked)
                },
            )

            collectJob.cancel()
        }

    @Test
    fun `공백 검색어를 입력하면 상태가 변경되지 않는다`() =
        runTest {
            viewModel =
                SearchTabViewModel(
                    bookSearchRepository = bookSearchRepository,
                    bookmarkRepository = bookmarkRepository,
                )

            viewModel.searchByKeyword("   ")
            advanceUntilIdle()

            val state = viewModel.stateFlow.value

            assertTrue(state is SearchTabState.Uninitialized)
        }

    @Test
    fun `검색 실패 시 Error 상태가 된다`() =
        runTest {
            coEvery {
                bookSearchRepository.searchBooksByKeyword(
                    query = "android",
                    startIndex = 0,
                    maxResults = 20,
                )
            } throws IllegalStateException("요청이 너무 많습니다.")

            viewModel =
                SearchTabViewModel(
                    bookSearchRepository = bookSearchRepository,
                    bookmarkRepository = bookmarkRepository,
                )

            viewModel.searchByKeyword("android")
            advanceUntilIdle()

            val state = viewModel.stateFlow.value

            assertTrue(state is SearchTabState.Error)

            val errorState = state as SearchTabState.Error
            assertEquals("요청이 너무 많습니다.", errorState.message)
            assertEquals("android", errorState.searchKeyword)
        }

    @Test
    fun `북마크되지 않은 책의 하트를 누르면 addBookmark가 호출된다`() =
        runTest {
            val book =
                BookModel(
                    id = "book-1",
                    title = "Android Clean Architecture",
                    subtitle = "Guide",
                    authors = listOf("John Doe"),
                    publisher = "Sample Publisher",
                    publishedDate = "2024-01-01",
                    description = "description",
                    thumbnail = "https://example.com/image.jpg",
                    previewLink = "https://example.com/preview",
                    infoLink = "https://example.com/info",
                    isLiked = false,
                )

            coEvery { bookmarkRepository.addBookmark(book) } returns Unit

            viewModel =
                SearchTabViewModel(
                    bookSearchRepository = bookSearchRepository,
                    bookmarkRepository = bookmarkRepository,
                )

            viewModel.toggleBookmark(book)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                bookmarkRepository.addBookmark(book)
            }
        }

    @Test
    fun `이미 북마크된 책의 하트를 누르면 removeBookmark가 호출된다`() =
        runTest {
            val book =
                BookModel(
                    id = "book-1",
                    title = "Android Clean Architecture",
                    subtitle = "Guide",
                    authors = listOf("John Doe"),
                    publisher = "Sample Publisher",
                    publishedDate = "2024-01-01",
                    description = "description",
                    thumbnail = "https://example.com/image.jpg",
                    previewLink = "https://example.com/preview",
                    infoLink = "https://example.com/info",
                    isLiked = true,
                )

            coEvery { bookmarkRepository.removeBookmark("book-1") } returns Unit

            viewModel =
                SearchTabViewModel(
                    bookSearchRepository = bookSearchRepository,
                    bookmarkRepository = bookmarkRepository,
                )

            viewModel.toggleBookmark(book)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                bookmarkRepository.removeBookmark("book-1")
            }
        }

    @Test
    fun `북마크 Flow가 변경되면 검색 결과의 isLiked가 다시 계산된다`() =
        runTest {
            val rawBooks =
                listOf(
                    BookModel(
                        id = "book-1",
                        title = "Android Clean Architecture",
                        subtitle = "Guide",
                        authors = listOf("John Doe"),
                        publisher = "Sample Publisher",
                        publishedDate = "2024-01-01",
                        description = "description",
                        thumbnail = "https://example.com/image.jpg",
                        previewLink = "https://example.com/preview",
                        infoLink = "https://example.com/info",
                        isLiked = null,
                    ),
                )

            coEvery {
                bookSearchRepository.searchBooksByKeyword(
                    query = "android",
                    startIndex = 0,
                    maxResults = 20,
                )
            } returns rawBooks

            viewModel =
                SearchTabViewModel(
                    bookSearchRepository = bookSearchRepository,
                    bookmarkRepository = bookmarkRepository,
                )

            viewModel.searchByKeyword("android")
            advanceUntilIdle()

            var state = viewModel.stateFlow.value as SearchTabState.Success
            assertEquals(false, state.books.first().isLiked)

            bookmarkFlow.value =
                listOf(
                    BookModel(
                        id = "book-1",
                        title = "Android Clean Architecture",
                        subtitle = "Guide",
                        authors = listOf("John Doe"),
                        publisher = "Sample Publisher",
                        publishedDate = "2024-01-01",
                        description = "description",
                        thumbnail = "https://example.com/image.jpg",
                        previewLink = "https://example.com/preview",
                        infoLink = "https://example.com/info",
                        isLiked = true,
                    ),
                )
            advanceUntilIdle()

            state = viewModel.stateFlow.value as SearchTabState.Success
            assertEquals(true, state.books.first().isLiked)
        }
}
