package com.subin.cleanbookstore.core


sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class  Error(val exception: Throwable) : DataResult<Nothing>
}

