package com.moon.cleanbookstore.data.remote.response

import com.google.gson.annotations.SerializedName

data class BookSearchResponse(
    @SerializedName("totalItems") val totalItems: Int?,
    @SerializedName("items") val items: List<BookItemDto>?
)

// 책 한 권의 데이터
data class BookItemDto(
    @SerializedName("id") val id: String, // Google API 고유 ID
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfoDto?,
    @SerializedName("accessInfo") val accessInfo: AccessInfoDto?
)

// 책의 상세 정보 (제목, 작가, 이미지 등)
data class VolumeInfoDto(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("authors") val authors: List<String>?,
    @SerializedName("description") val description: String?,
    @SerializedName("imageLinks") val imageLinks: ImageLinksDto?
)

// 썸네일 이미지 링크
data class ImageLinksDto(
    @SerializedName("smallThumbnail") val smallThumbnail: String?,
    @SerializedName("thumbnail") val thumbnail: String?
)

// PDF 등 읽기 권한 및 링크 정보
data class AccessInfoDto(
    @SerializedName("pdf") val pdf: PdfDto?
)

// 실제 PDF 다운로드/읽기 링크
data class PdfDto(
    @SerializedName("isAvailable") val isAvailable: Boolean?,
    @SerializedName("acsTokenLink") val acsTokenLink: String?
)