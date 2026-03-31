package com.moon.cleanbookstore.domain.model

data class SearchHistory(
    val keyword: String,       // 검색어 (기존 text 역할)
    val timestamp: Long        // 검색한 시간 (정렬 및 관리를 위해 추가 추천)
)