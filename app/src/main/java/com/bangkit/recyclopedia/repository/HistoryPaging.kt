package com.bangkit.recyclopedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bangkit.recyclopedia.api.ApiService
import com.bangkit.recyclopedia.api.response.HistoryItemsResponse

class HistoryPaging(private val apiService: ApiService, private val token: String): PagingSource<Int, HistoryItemsResponse>() {
    override fun getRefreshKey(state: PagingState<Int, HistoryItemsResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HistoryItemsResponse> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getHistory(position, params.loadSize)

            LoadResult.Page(
                data = responseData.data,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.data.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}