package com.subin.cleanbookstore

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CleanBookStoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}