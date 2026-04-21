package com.moon.cleanbookstore

import android.app.Application
import com.moon.cleanbookstore.di.AppContainer

class BookStoreApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}