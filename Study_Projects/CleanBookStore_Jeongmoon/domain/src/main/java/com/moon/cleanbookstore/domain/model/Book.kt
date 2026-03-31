package com.moon.cleanbookstore.domain.model

data class Book(
    val id: String,               // 고유 ID
    val title: String,            // 책 제목
    val subtitle: String,         // 부제목
    val authors: List<String>,    // 저자 목록
    val description: String,      // 책 설명
    val imageUrl: String,         // 썸네일 이미지
    val pdfLink: String,          // PDF 다운로드/읽기 링크
    val isBookmarked: Boolean = false // 북마크(좋아요) 상태
)