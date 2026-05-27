# BookSearch-CleanArchitecture

[![Android CI](https://github.com/ksubin-dev/BookSearch-CleanArchitecture/actions/workflows/android-ci.yml/badge.svg)](https://github.com/ksubin-dev/BookSearch-CleanArchitecture/actions/workflows/android-ci.yml)

> **Clean Architecture와 Google Books API를 활용한 안드로이드 도서 검색 앱**

<br/>

## 📌 프로젝트 소개

**BookSearch-CleanArchitecture**는 Google Books API를 활용하여 도서를 검색하고, 원하는 도서를 북마크할 수 있는 안드로이드 앱입니다.

이 프로젝트는 단순히 API 데이터를 화면에 출력하는 것에 그치지 않고, **Clean Architecture**, **MVVM**, **Paging 3**, **Room**, **Hilt**, **Flow**를 적용하여 계층 간 책임을 분리하고 유지보수 가능한 구조를 만드는 것을 목표로 진행했습니다.

<br/>

## 🏗 아키텍처

이 프로젝트는 Clean Architecture 기반으로 `Presentation`, `Domain`, `Data` 계층을 분리했습니다.

```text
Presentation Layer
- Jetpack Compose UI
- ViewModel
- UI State 관리
- 사용자 이벤트 처리

Domain Layer
- UseCase
- Repository Interface
- Domain Model
- 비즈니스 로직

Data Layer
- Repository Implementation
- Retrofit API
- PagingSource
- Room Database
- DTO / Entity / Mapper
```

### 계층별 역할

| Layer | 역할 |
| --- | --- |
| Presentation | 화면 렌더링, 사용자 이벤트 처리, ViewModel을 통한 상태 관리 |
| Domain | 앱의 핵심 비즈니스 로직과 순수 모델 관리 |
| Data | 외부 API, 로컬 DB, 데이터 매핑 및 Repository 구현 |

<br/>

## 📂 프로젝트 구조

```text
com.subin.cleanbookstore/
├── core/
│   └── 공통 상태 처리 래퍼
│
├── data/
│   ├── di/
│   │   └── Data 계층 의존성 주입 모듈
│   ├── local/
│   │   └── Room DB, Dao, Entity
│   ├── mapper/
│   │   └── DTO / Entity ↔ Domain Model 변환
│   ├── remote/
│   │   └── Retrofit API, DTO, PagingSource
│   └── repository/
│       └── Repository 구현체
│
├── di/
│   └── RepositoryModule 등 전체 의존성 주입
│
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│
└── presentation/
    ├── bookmark/
    ├── search/
    └── ui/
```

<br/>

## ✨ 주요 기능

### 1. 도서 검색

Google Books API를 통해 사용자가 입력한 키워드에 해당하는 도서 목록을 조회합니다.

- 키워드 기반 도서 검색
- 검색 결과 리스트 표시
- 도서 제목, 저자, 썸네일 등 기본 정보 제공
- 로딩 / 에러 / 빈 결과 상태 처리

<br/>

### 2. 최근 검색어 저장

사용자가 검색한 키워드를 로컬 DB에 저장하여 최근 검색어로 제공합니다.

- 최근 검색어 자동 저장
- 최근 검색어 개별 삭제
- 최근 검색어 전체 삭제
- Room을 활용한 로컬 데이터 관리

<br/>

### 3. 무한 스크롤

Paging 3를 적용하여 검색 결과를 페이지 단위로 불러옵니다.

- Google Books API 페이지네이션 연동
- 스크롤 시 추가 데이터 로드
- 초기 로딩 / 추가 로딩 / 에러 상태 처리
- 불규칙한 API 응답 개수에 대응

<br/>

### 4. 북마크 기능

도서를 북마크에 추가하거나 제거할 수 있습니다.

- 북마크 추가
- 북마크 해제
- 북마크 목록 조회
- Room DB 기반 로컬 저장
- 검색 결과 화면과 북마크 상태 실시간 동기화

<br/>

## 🎥 화면 시연

| 🔍 최근 검색어 관리 | 📜 무한 스크롤 (Paging 3) | 💖 북마크 실시간 동기화 |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/f130a5c5-6ca1-4b96-957a-4fa75a6b7392" width="180" /> | <img src="https://github.com/user-attachments/assets/b63c753a-e4aa-49bb-97e8-62aa47bc5775" width="180" /> | <img src="https://github.com/user-attachments/assets/e672032b-46f2-4429-90b1-d91804c3bcfc" width="180" /> |
| 로컬 DB를 활용한<br>검색어 자동 저장 및 삭제 | Google Books API와 연동한<br>대량 데이터 파이프라인 처리 | Flow.combine을 활용한<br>완벽한 화면 상태 동기화 |


<br/>

## 🛠 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material Design 3 |
| Architecture | Clean Architecture, MVVM |
| Asynchronous | Coroutine, Flow |
| Paging | Paging 3 |
| Network | Retrofit2, OkHttp3 |
| Local Database | Room |
| Dependency Injection | Hilt |
| API | Google Books API |
| CI | GitHub Actions |

<br/>

## 🔄 데이터 흐름

```text
User Event
   ↓
Compose UI
   ↓
ViewModel
   ↓
UseCase
   ↓
Repository Interface
   ↓
Repository Implementation
   ↓
Remote API / Local DB
   ↓
Mapper
   ↓
Domain Model
   ↓
UI State
```

<br/>

## 💡 트러블 슈팅

### 1. Paging 3 조기 종료 문제

#### 문제

Google Books API는 요청한 `loadSize`보다 적은 개수의 데이터를 반환하는 경우가 있었습니다.

기존에는 응답 데이터 개수가 `params.loadSize`보다 작으면 마지막 페이지로 판단하도록 처리했기 때문에, 실제로는 더 불러올 데이터가 남아 있음에도 Paging이 첫 페이지 또는 중간 페이지에서 조기 종료되는 문제가 발생했습니다.

#### 해결

`books.size < params.loadSize` 조건을 제거하고, 응답 결과가 완전히 비어 있는 경우에만 페이징을 종료하도록 수정했습니다.

```kotlin
nextKey = if (books.isEmpty()) {
    null
} else {
    currentPage + books.size
}
```

#### 결과

불규칙한 응답 개수에도 다음 페이지를 안정적으로 요청할 수 있게 되었고, 무한 스크롤이 중간에 끊기는 문제가 해결되었습니다.

<br/>

<br/>

### 2. PagingData와 북마크 상태 동기화 문제

#### 문제

Paging 3를 도입하면서 검색 결과 타입이 기존 단발성 데이터 구조에서 `Flow<PagingData<Book>>` 구조로 변경되었습니다.

이로 인해 로컬 DB에 저장된 북마크 상태와 API에서 받아온 검색 결과를 어떻게 동기화할지 문제가 발생했습니다.

#### 해결

검색 결과 스트림과 북마크 목록 스트림을 함께 관찰하고, 각 도서의 북마크 여부를 UI 모델에 반영하도록 처리했습니다.

```kotlin
combine(
    pagingDataFlow,
    bookmarkedBooksFlow
) { pagingData, bookmarkedBooks ->
    pagingData.map { book ->
        book.copy(
            isBookmarked = bookmarkedBooks.any { it.id == book.id }
        )
    }
}
```

#### 결과

검색 결과 화면에서도 북마크 추가 및 해제 상태가 즉시 반영되도록 개선했습니다.

<br/>

## 🧩 핵심 구현 포인트

### Clean Architecture 적용

- UI, 비즈니스 로직, 데이터 접근 책임 분리
- Domain Layer가 Android Framework에 의존하지 않도록 구성
- Repository Interface를 Domain에 두고 구현체는 Data Layer에서 담당

<br/>

### Repository 패턴

- ViewModel은 데이터 출처를 직접 알지 않음
- API와 DB 접근 로직을 Repository에서 캡슐화
- 테스트 및 유지보수에 유리한 구조 확보

<br/>

### Flow 기반 상태 관리

- 검색어, 검색 결과, 북마크 목록을 Flow 기반으로 관리
- 데이터 변경 사항을 UI에 실시간 반영
- 비동기 스트림을 ViewModel에서 조합하여 UI State 생성

<br/>

## 📚 배운 점

- Clean Architecture에서 각 계층의 책임을 분리하는 기준을 이해할 수 있었습니다.
- Paging 3를 실제 API와 연동하면서 페이지 키 설계의 중요성을 경험했습니다.
- 외부 API 데이터가 항상 안정적이거나 고유하다고 가정하면 안 된다는 점을 배웠습니다.
- Room과 Flow를 함께 사용하여 로컬 데이터 변경을 UI에 실시간 반영하는 방법을 익혔습니다.
- 단순 기능 구현보다 데이터 흐름과 상태 관리 구조를 먼저 설계하는 것이 중요하다는 점을 알게 되었습니다.

<br/>

## 🚀 실행 방법

### 1. Repository Clone

```bash
git clone https://github.com/ksubin-dev/BookSearch-CleanArchitecture.git
```

### 2. Android Studio에서 프로젝트 열기

Android Studio에서 프로젝트를 열고 Gradle Sync를 진행합니다.

### 3. API Key 설정

`local.properties` 파일에 Google Books API Key를 추가합니다.

```properties
BOOKS_API_KEY=YOUR_API_KEY
```

> 프로젝트의 실제 API Key 관리 방식에 따라 변수명은 수정해서 사용하면 됩니다.

### 4. 앱 실행

Android Emulator 또는 실제 기기에서 앱을 실행합니다.

<br/>

## 📌 향후 개선 사항
- 북마크 정렬 및 검색 기능 추가
- 네트워크 연결 상태별 UI 개선
- 테스트 코드 추가
- 다크 모드 UI 개선

<br/>

## 📝 License

This project is for personal study and portfolio purposes.
