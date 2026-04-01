package com.jeong.cleanbookstore.screen.main.search

import com.jeong.cleanbookstore.data.repository.BookSearchRepository
import com.jeong.cleanbookstore.model.book.BookModel
import com.jeong.cleanbookstore.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherRule::class)
class SearchTabViewModelTest {
    private lateinit var bookSearchRepository: BookSearchRepository
    private lateinit var viewModel: SearchTabViewModel

    @BeforeEach
    fun setUp() {
        bookSearchRepository = mockk()
        viewModel = SearchTabViewModel(bookSearchRepository)
    }

    @Test
    fun `초기 상태는 Uninitialized이다`() =
        runTest {
            val state = viewModel.stateFlow.value
            assertTrue(state is SearchTabState.Uninitialized)
        }

    @Test
    fun `fetchData를 호출하면 빈 Success 상태가 된다`() =
        runTest {
            viewModel.fetchData()
            advanceUntilIdle()

            val state = viewModel.stateFlow.value
            assertTrue(state is SearchTabState.Success)

            val successState = state as SearchTabState.Success
            assertTrue(successState.books.isEmpty())
            assertEquals("", successState.searchKeyword)
        }

    @Test
    fun `검색 성공 시 Success 상태와 검색 결과가 반영된다`() =
        runTest {
            val books =
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
                        isLiked = false,
                    ),
                )

            coEvery {
                bookSearchRepository.searchBooksByKeyword(
                    query = "android",
                    startIndex = 0,
                    maxResults = 20,
                )
            } returns books

            viewModel.searchByKeyword("android")
            advanceUntilIdle()

            val state = viewModel.stateFlow.value
            assertTrue(state is SearchTabState.Success)

            val successState = state as SearchTabState.Success
            assertEquals("android", successState.searchKeyword)
            assertEquals(1, successState.books.size)
        }

    @Test
    fun `공백 검색어를 입력하면 상태가 변경되지 않는다`() =
        runTest {
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

            viewModel.searchByKeyword("android")
            advanceUntilIdle()

            val state = viewModel.stateFlow.value
            assertTrue(state is SearchTabState.Error)

            val errorState = state as SearchTabState.Error
            assertEquals("요청이 너무 많습니다.", errorState.message)
            assertEquals("android", errorState.searchKeyword)
        }
}
