# Android 4대 컴포넌트 Part 4 — ContentProvider

## 전체 핵심 요약

- ContentProvider는 **앱 간 데이터 공유를 표준화**한 컴포넌트다.
- 외부 앱은 ContentProvider의 데이터를 **ContentResolver**를 통해 `query/insert/update/delete`로 접근한다.
- 데이터 주소는 **content://authority/path/id** 형태의 **URI**로 표현된다.
- 보안이 핵심: `exported`, `read/write permission`, URI 권한(grant), 공개 범위를 최소화하는 설계가 중요.

---

# 1) ContentProvider가 하는 일 (정의/역할)

## 핵심

- 앱 내부 데이터(DB/파일 등)를 다른 앱과 공유할 수 있게 표준 인터페이스로 감싼 컴포넌트
- 다른 앱은 직접 DB 파일을 못 열고, Provider가 제공하는 API로만 접근 가능

---

# 2) ContentResolver와의 관계

## 핵심

- **Provider는 제공자**, **Resolver는 소비자(접근 API)**

Android에서 외부/다른 모듈이 Provider 데이터를 읽을 때 보통 이 흐름:

- `contentResolver.query(uri, ...)`
- `contentResolver.insert(uri, values)`
- `contentResolver.update(uri, values, ...)`
- `contentResolver.delete(uri, ...)`

---

# 3) URI 구조 (가장 중요)

## 3-1) 기본 형태

- `content://<authority>/<path>/<id?>`

예:

- `content://com.example.app.provider/users`
- `content://com.example.app.provider/users/10`

### 각 요소 의미

- **scheme**: `content://`
- **authority**: Provider 식별자(보통 패키지 기반)
- **path**: 데이터 테이블/리소스 경로(예: users)
- **id**: 특정 레코드 식별(옵션)

---

## 3-2) 컬렉션 vs 단일 아이템 URI 구분

- `.../users` : 전체 목록(컬렉션)
- `.../users/10` : 특정 아이템(단일)

이 구분이 CRUD 로직에서 매우 중요(URI matcher로 분기)

---

# 4) MIME 타입

Provider는 `getType(uri)`로 URI가 가리키는 데이터의 타입을 반환할 수 있습니다.

- 목록(여러 개): `vnd.android.cursor.dir/...`
- 단일(한 개): `vnd.android.cursor.item/...`

---

# 5) CRUD 동작 (query/insert/update/delete)

## 핵심

Provider가 구현하는 대표 메서드:

- `query()`: 조회 (Cursor 반환)
- `insert()`: 삽입 (삽입된 row의 URI 반환)
- `update()`: 수정 (영향 받은 row 수 반환)
- `delete()`: 삭제 (영향 받은 row 수 반환)

> 외부는 함수 호출이 아니라, URI 기반으로 접근 → Provider가 내부에서 DB/파일 조작
> 

---

# 6) 권한/보안 (핵심)

## 6-1) exported(공개 여부)

- 외부 앱이 접근 가능하게 하려면 exported 관련 설정이 중요
- 내부 전용이면 기본적으로 외부 접근을 막는 설계를 선호

## 6-2) read/write permission

- `readPermission`, `writePermission`으로 접근 통제 가능
- 민감 데이터는 권한 없이는 접근 불가능하게

## 6-3) URI grant (임시 권한 부여)

- 특정 앱에게 **이 URI에 한해서** 읽기/쓰기 권한을 잠깐 주는 방식
- 파일 공유에서 특히 중요(“내 앱 파일을 다른 앱에 넘겨야 할 때”)

---

# 7) 대표 케이스 3개

## 케이스 A) 시스템 Provider 사용(읽기)

- Contacts(연락처), Calendar, MediaStore(사진/동영상) 등
- 우리는 Provider를 만드는 게 아니라 Resolver로 접근하는 쪽이 많음

## 케이스 B) 파일 공유(공유하기)

- 사진 첨부, 다른 앱으로 파일 전달
- **FileProvider(Provider 기반)** 형태로 자주 접함 (키워드)

## 케이스 C) 앱 간 데이터 공유(특수)

- 회사 내부 앱들 간 공유, 플러그인 구조 등
- 흔치는 않지만 Provider는 표준 방식

---

# 8) Android 예제: 시스템 Provider 접근 (Resolver로 조회)

> 대표적으로 MediaStore에서 이미지 목록 조회 같은 케이스가 Provider 사용의 체감 포인트입니다.
> 

```kotlin
val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
val projection = arrayOf(
    MediaStore.Images.Media._ID,
    MediaStore.Images.Media.DISPLAY_NAME
)

val cursor = contentResolver.query(
    uri,
    projection,
    null,
    null,
    null
)

cursor?.use {
    val idIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
    val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

    while (it.moveToNext()) {
        val id = it.getLong(idIndex)
        val name = it.getString(nameIndex)
        // id/name 사용
    }
}
```

✅ 포인트

- `contentResolver.query()` → Cursor 기반 조회
- Provider를 “사용하는 방식”을 이해하는 게 중요

---

# 9) (개념) Provider 구현이 어떻게 생겼는지 최소 구조만 보기

```kotlin
class MyProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? {
        // uri 분석 → DB query → Cursor 반환
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}
```
