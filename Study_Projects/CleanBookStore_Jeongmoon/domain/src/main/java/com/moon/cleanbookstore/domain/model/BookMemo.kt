package com.moon.cleanbookstore.domain.model

data class BookMemo(
    val id: String,          // Google Books 고유 ID
    val memoContent: String, // 사용자가 작성한 메모 내용
    val lastModified: Long   // 메모를 작성/수정한 시간
)