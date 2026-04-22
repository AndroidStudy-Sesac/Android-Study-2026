package com.moon.cleanbookstore.domain.model

data class SearchHistory(
    val keyword: String,       // 검색어
    val timestamp: Long        // 검색한 시간
)