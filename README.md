# Modern Android & CS Study

안드로이드 개발과 CS 기초를 함께 학습하고 기록하는 스터디 저장소입니다.  
각자 학습한 개념을 문서로 정리하고, 학습 내용을 Android 프로젝트에 적용하며 실습 중심으로 성장하는 것을 목표로 합니다.

<br>

## 📌 Study Goals

- Android 개발 핵심 개념 학습
- Kotlin, Jetpack Compose, Android Architecture 이해
- CS 및 개발 기초 개념 문서화
- 학습 내용을 개인/공통 프로젝트에 적용
- GitHub Issue, Branch, Pull Request 기반 협업 연습
- 코드 리뷰와 문서 리뷰를 통한 개발 습관 개선

<br>

## 📁 Repository Structure

```text
Android-Study-2026
├── Concepts
│   ├── session1
│   ├── session2
│   └── session3
│
├── Study_Projects
│   ├── ComposeAuth_Subin
│   ├── ComposeAuth_Jiyeong
│   ├── ComposeAuth_Jeongmoon
│   ├── CleanBookstore_Jiyeong
│   ├── CleanBookStore_Jeongmoon
│   └── BookSearch_CleanArchitecture_Subin
│
├── .gitignore
└── README.md
```

<br>

## 📚 Concepts

`Concepts` 폴더는 스터디에서 학습한 개념을 정리하는 공간입니다.  
개념 문서는 반드시 회차별 폴더 안에 작성합니다.

```text
Concepts
└── session1
    ├── subin
    │   └── 1_Lifecycle.md
    ├── jiyoung
    └── jeongmoon
```

### 작성 규칙

- 루트 경로에 개념 문서를 바로 추가하지 않습니다.
- `Concepts/session회차/이름` 구조를 유지합니다.
- 파일명은 학습 주제가 드러나도록 작성합니다.

예시:

```text
Concepts/session1/subin/1_Lifecycle.md
Concepts/session2/jiyoung/2_Coroutine.md
Concepts/session3/jeongmoon/3_Room.md
```

<br>

## 🧩 Study_Projects

`Study_Projects` 폴더는 스터디 실습용 Android 프로젝트를 정리하는 공간입니다.  
각 프로젝트는 독립적인 Android Studio 프로젝트로 관리합니다.

```text
Study_Projects
├── ComposeAuth_Subin
├── ComposeAuth_Jiyeong
├── ComposeAuth_Jeongmoon
└── BookSearch_CleanArchitecture_Subin
```

### Android Studio 실행 시 주의사항

Android Studio에서 프로젝트를 열 때는 저장소 최상단이 아니라,  
본인이 작업할 개별 프로젝트 폴더를 직접 열어야 합니다.

예시:

```text
Study_Projects/ComposeAuth_Subin
Study_Projects/BookSearch_CleanArchitecture_Subin
```

저장소 최상단 `Android-Study-2026`을 바로 열면 Gradle 설정이 충돌하거나 프로젝트 인식 오류가 발생할 수 있습니다.

<br>

## 🌿 Branch Convention

작업을 시작하기 전 반드시 `main` 브랜치를 최신화한 뒤 새 브랜치를 생성합니다.

```bash
git checkout main
git pull origin main
git checkout -b 브랜치명
```

| 작업 유형 | 브랜치 형식 | 예시 |
| --- | --- | --- |
| 개념 정리 | `session회차/이름` | `session1/subin` |
| 공통 프로젝트 기능 작업 | `feat/프로젝트명/이름` | `feat/compose-auth/subin` |
| 개인 프로젝트 작업 | `feat/프로젝트명/이름` | `feat/book-search/subin` |
| 문서 수정 | `docs/작업내용` | `docs/readme-cleanup` |
| 버그 수정 | `fix/프로젝트명/작업내용` | `fix/compose-auth/login-error` |
| 리팩토링 | `refactor/프로젝트명/작업내용` | `refactor/book-search/data-layer` |

<br>

## ✅ Commit Convention

커밋 메시지는 아래 형식을 사용합니다.

```text
태그: [분류] 작업 내용
```

예시:

```text
docs: [session1] 수빈 Lifecycle 개념 정리
feat: [ComposeAuth_Subin] 로그인 화면 구현
feat: [BookSearch_CleanArchitecture_Subin] 도서 검색 앱 프로젝트 추가
fix: [ComposeAuth_Jiyeong] 회원가입 입력 검증 오류 수정
refactor: [CleanBookstore_Jiyeong] data layer 구조 정리
chore: [Setting] README 규칙 정리
```

| 태그 | 의미 |
| --- | --- |
| `docs` | 문서 작성 및 수정 |
| `feat` | 새로운 기능 또는 프로젝트 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변화 없는 코드 구조 개선 |
| `chore` | 설정, 폴더 생성, 기타 작업 |
| `test` | 테스트 코드 추가 및 수정 |
| `style` | 코드 포맷팅, UI 스타일 수정 |

<br>

## 🔀 Pull Request Convention

모든 작업은 `main` 브랜치로 Pull Request를 생성한 뒤 병합합니다.  
`main` 브랜치에 직접 Push하지 않습니다.

### PR 제목

PR 제목은 커밋 메시지와 같은 형식을 사용합니다.

```text
feat: [ComposeAuth_Subin] 로그인 화면 구현
docs: [session2] 지영 Coroutine 개념 정리
feat: [BookSearch_CleanArchitecture_Subin] 도서 검색 앱 프로젝트 추가
```

### PR 본문

```md
## 작업 내용
- 작업한 내용을 요약합니다.

## 확인 사항
- 실행 확인, 문서 확인, 테스트 여부 등을 작성합니다.

## Related
- Related #이슈번호
```

이슈를 PR 병합과 함께 닫고 싶을 때는 아래처럼 작성합니다.

```md
## Related
- Closes #이슈번호
```

<br>

## 🔍 Review & Merge Rule

1. PR은 `main` 브랜치를 대상으로 생성합니다.
2. PR 생성 전, 가능하면 최신 `main`을 작업 브랜치에 반영합니다.
3. 스터디 시간에 각자의 코드와 문서를 함께 리뷰합니다.
4. 최소 1명 이상의 팀원에게 확인받은 뒤 Merge합니다.
5. 독단적으로 `main` 브랜치에 직접 Push하지 않습니다.

<br>

## 🛠 Android Project Rule

Android 프로젝트를 추가할 때는 아래 형식을 권장합니다.

```text
Study_Projects/프로젝트명_이름
```

예시:

```text
Study_Projects/ComposeAuth_Subin
Study_Projects/BookSearch_CleanArchitecture_Subin
```

### 커밋하지 않는 파일 및 폴더

개인 로컬 설정, 빌드 산출물, IDE 설정 파일은 커밋하지 않습니다.

```text
.gradle
.idea
build
app/build
local.properties
```

### 권장 포함 파일

Android 프로젝트 제출 시 아래 파일은 포함합니다.

```text
app
gradle
.gitignore
build.gradle.kts
gradle.properties
gradlew
gradlew.bat
settings.gradle.kts
README.md
```

<br>

## 🧭 Study Flow

1. 주차별 학습 주제를 정합니다.
2. 각자 `Concepts/session회차/이름` 폴더에 개념을 정리합니다.
3. 실습이 필요한 내용은 `Study_Projects` 안의 프로젝트에 적용합니다.
4. 작업 브랜치에서 커밋 후 Pull Request를 생성합니다.
5. 스터디 시간에 문서와 코드를 함께 리뷰합니다.
6. 리뷰 완료 후 `main` 브랜치에 Merge합니다.

<br>

## 📌 Current Study Projects

| Project | Description |
| --- | --- |
| `ComposeAuth` | Jetpack Compose 기반 로그인/회원가입 실습 프로젝트 |
| `CleanBookStore` | 도서 검색 앱 구조화 및 아키텍처 실습 프로젝트 |
| `BookSearch_CleanArchitecture` | Clean Architecture와 Google Books API를 활용한 도서 검색 앱 프로젝트 |

<br>

## 💬 Notes

이 저장소는 개인 학습 기록뿐만 아니라 협업 방식도 함께 연습하기 위한 공간입니다.  
문서화, 브랜치 전략, 커밋 컨벤션, PR 리뷰 과정을 꾸준히 지키는 것을 목표로 합니다.
