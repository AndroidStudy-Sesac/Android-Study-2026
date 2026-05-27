package com.subin.cleanbookstore.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.subin.cleanbookstore.data.mapper.toDomain
import com.subin.cleanbookstore.data.remote.api.BooksApiService
import com.subin.cleanbookstore.domain.model.Book
import retrofit2.HttpException
import java.io.IOException

class BookPagingSource(
    private val apiService: BooksApiService,
    private val query: String
) : PagingSource<Int, Book>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val position = params.key ?: 0

        return try {
            val response = apiService.searchBooks(
                query = query,
                startIndex = position,
                maxResults = params.loadSize
            )

            val books = response.items?.map { it.toDomain() } ?: emptyList()

            val nextKey = if (books.isEmpty()) {
                null
            } else {
                position + params.loadSize
            }

            LoadResult.Page(
                data = books,
                prevKey = if (position == 0) null else position - params.loadSize,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        }
    }
}