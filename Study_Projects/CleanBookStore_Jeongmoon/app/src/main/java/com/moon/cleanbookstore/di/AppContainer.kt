package com.moon.cleanbookstore.di

import android.content.Context
import com.moon.cleanbookstore.data.di.DataContainer
import com.moon.cleanbookstore.feature.di.AppViewModelFactory

class AppContainer(private val context: Context) {

    private val dataContainer = DataContainer(context = context)

    val viewModelFactory: AppViewModelFactory by lazy {
        AppViewModelFactory(
            bookStoreRepository = dataContainer.bookStoreRepository,
            bookSearchRepository = dataContainer.bookSearchRepository,
            bookMemoRepository = dataContainer.bookMemoRepository
        )
    }
}