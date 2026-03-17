# Android 스터디 1회차 — Part C

## Clean Architecture + Design Patterns (Builder/Singleton/DI/Factory/Observer/Repository + Architecture 연결)

---

## 전체 핵심 요약

- **Clean Architecture**는 “레이어 분리 + 의존성 방향(안쪽으로만)”으로 유지보수/테스트를 쉽게 만드는 구조다.
- Android에서는 보통 **Presentation(ViewModel)** / **Domain(UseCase)** / **Data(RepositoryImpl + DataSource)** 로 나누어 적용한다.
- Design Pattern은 자주 반복되는 문제의 해결 템플릿이며, Android에서는 아래가 특히 자주 쓰인다:
    - **Builder**: 객체 설정/생성(예: `AlertDialog.Builder`, `Bundle.apply`)
    - **Singleton**: 전역 1개 인스턴스(주의 필요)
    - **DI(Dependency Injection)**: 의존성 주입(테스트/교체/결합도 낮춤)
    - **Factory**: 생성 로직 캡슐화
    - **Observer**: 변화 구독(LiveData/Flow)
    - **Repository**: 데이터 접근 단일 창구(네트워크/DB 추상화)
- 아키텍처(MVVM/MVP/MVI/Clean)를 “패턴 목록”과 연결해 **면접 답변 형태**로 정리하는 게 목적

---

# 1) Clean Architecture

## 1-1. 핵심 개념

- 시스템을 여러 레이어로 나누고
- **의존성은 항상 안쪽(도메인) 방향으로만** 흐르게 만든다.

> 바깥(UI, 프레임워크)은 자주 바뀌고,
> 
> 
> 안쪽(도메인 규칙)은 최대한 안정적으로 유지한다.
> 

---

## 1-2. Android에서 흔한 3레이어

### Presentation

- Activity/Fragment/Compose
- ViewModel
- UI State, UI Event

### Domain

- UseCase(Interactor)
- Entity(도메인 모델)
- Repository “인터페이스”(계약)

### Data

- Repository 구현체(Impl)
- RemoteDataSource(네트워크), LocalDataSource(DB)
- DTO/Entity 매핑(Mapper)

---

## 1-3. 의존성 규칙(Dependency Rule)

- Presentation → Domain ← Data
    - Data는 Domain의 **인터페이스를 구현**하는 방식으로 의존성을 “안쪽으로” 맞춘다.

---

## Android 실무 예제

### (1) Domain: Repository 계약 + UseCase

```kotlin
// Domain
interface UserRepository {
    suspend fun getUserName(userId: Long): String
}

class GetUserNameUseCase(
    private val repo: UserRepository
) {
    suspend operator fun invoke(userId: Long): String {
        return repo.getUserName(userId)
    }
}
```

### (2) Data: Repository 구현 + DataSource

```kotlin
// Data
class UserRepositoryImpl(
    private val remote: UserRemoteDataSource
) : UserRepository {
    override suspend fun getUserName(userId: Long): String {
        return remote.fetchUserName(userId)
    }
}

class UserRemoteDataSource {
    suspend fun fetchUserName(userId: Long): String {
        // 네트워크 호출이라고 가정
        return "Heewon"
    }
}
```

### (3) Presentation: ViewModel에서 UseCase 호출

```kotlin
// Presentation
class ProfileViewModel(
    private val getUserName: GetUserNameUseCase
) : ViewModel() {

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    fun load(userId: Long) {
        viewModelScope.launch {
            _name.value = getUserName(userId)
        }
    }
}
```

> ✅ 포인트
> 
> 
> UI는 Repository 구현을 몰라도 되고(ViewModel은 UseCase만 앎)
> 
> Data는 Domain의 계약(UserRepository)을 구현함으로써 의존성 방향을 지킨다.
> 

---

## ✅ 기술면접 체크(질문+답)

### Q1. Clean Architecture의 핵심 한 줄?

**A.**

- “레이어를 나누고 의존성을 안쪽(도메인)으로만 향하게 만들어 테스트와 변경에 강하게 만든다.”

### Q2. Domain 레이어에 Android 의존 코드가 있으면 안 되는 이유?

**A.**

- Domain은 비즈니스 규칙의 중심이라 프레임워크 변경에 영향받지 않아야 하고,
- 테스트/재사용성을 높이기 위해 Android 의존성을 제거하는 게 원칙.

### Q3. Repository 패턴과 Clean Architecture는 어떻게 연결되나요?

**A.**

- Domain에 Repository “인터페이스”를 두고, Data가 이를 구현하여 의존성 규칙을 지키는 대표 구조다.

---

# 2) Repository 패턴 (Data Access Layer의 표준)

## 핵심

- 데이터 접근을 한 곳으로 모아 **UI/Domain이 데이터 소스를 몰라도 되게** 만든다.
- 네트워크/DB 캐싱 전략이 바뀌어도, Repository 인터페이스는 유지 가능

---

## Android 실무 예제: Remote + Local 조합(캐싱 느낌)

```kotlin
class UserRepositoryImpl(
    private val remote: UserRemoteDataSource,
    private val local: UserLocalDataSource
) : UserRepository {

    override suspend fun getUserName(userId: Long): String {
        val cached = local.getCachedName(userId)
        if (cached != null) return cached

        val fresh = remote.fetchUserName(userId)
        local.saveName(userId, fresh)
        return fresh
    }
}

class UserLocalDataSource {
    private val cache = mutableMapOf<Long, String>()
    fun getCachedName(id: Long): String? = cache[id]
    fun saveName(id: Long, name: String) { cache[id] = name }
}
```

---

## ✅ 기술면접 체크(질문+답)

### Q4. Repository를 왜 쓰나요?

**A.**

- UI/Domain이 네트워크/DB 등 구현 디테일을 몰라도 되게 추상화하고,
- 데이터 소스 변경/캐싱 전략 변경에 강하게 만들기 위해.

### Q5. DataSource를 왜 따로 두나요?

**A.**

- Remote/Local 책임을 분리해 테스트/교체가 쉬워지고,
- Repository는 “정책(캐싱/병합)”을 담당하게 만들 수 있음.

---

# 3) Dependency Injection (DI)

## 핵심

- 의존 객체를 클래스 내부에서 직접 만들지 않고 **외부에서 주입**한다.
- 결합도를 낮추고 테스트(Fake/Mock 교체)를 쉽게 한다.

---

## Android 실무 예제: “주입 vs 직접 생성” 차이

### ❌ 직접 생성 (테스트/교체 어려움)

```kotlin
class ProfileViewModel : ViewModel() {
    private val repo = UserRepositoryImpl(UserRemoteDataSource()) // 고정됨

    // ...
}
```

### ✅ 주입 (교체 가능)

```kotlin
class ProfileViewModel(
    private val repo: UserRepository
) : ViewModel() {
    // ...
}
```

---

## ✅ 기술면접 체크(질문+답)

### Q6. DI의 장점 3개?

**A.**

- 결합도 감소, 테스트 용이(Fake/Mock 교체), 생성 책임 분리(단일 책임)

### Q7. DI가 Clean Architecture에서 중요한 이유?

**A.**

- Domain 계약(인터페이스)에 맞는 구현체를 런타임에 주입해서 의존성 규칙을 유지하기 쉬움.

---

# 4) Factory 패턴

## 핵심

- 객체 생성 로직을 숨기고, “생성 정책”을 한 곳에 모은다.
- 복잡한 생성(검증/정규화/캐싱)이 있을 때 효과적

---

## Android 실무 예제 1: ViewModelFactory (전통적 패턴)

```kotlin
class ProfileViewModelFactory(
    private val repo: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

> 요즘은 DI(Hilt)로 대체되는 경우가 많지만, **면접 단골**이라 알아두는 게 좋음.
> 

---

## Android 실무 예제 2: Kotlin 스타일 Factory (companion object)

```kotlin
class Nickname private constructor(val value: String) {
    companion object {
        fun from(raw: String): Nickname {
            require(raw.length in 2..10)
            return Nickname(raw.trim())
        }
    }
}
```

---

## ✅ 기술면접 체크(질문+답)

### Q8. Factory를 쓰는 이유는?

**A.**

- 생성 로직을 캡슐화하고, 생성 규칙/검증을 강제하며, 호출부를 단순하게 만들기 위해.

---

# 5) Builder 패턴

## 핵심

- 복잡한 객체를 단계적으로 설정하고 생성한다.
- Android에서는 Builder가 매우 흔함.

---

## Android 실무 예제 1: AlertDialog.Builder

```kotlin
AlertDialog.Builder(requireContext())
    .setTitle("확인")
    .setMessage("삭제하시겠습니까?")
    .setPositiveButton("삭제") { _, _ -> /* delete */ }
    .setNegativeButton("취소", null)
    .show()
```

## Android 실무 예제 2: Kotlin `apply`는 Builder 느낌을 자주 만든다

```kotlin
val intent = Intent(requireContext(), DetailActivity::class.java).apply {
    putExtra("user_id", 1L)
}
startActivity(intent)
```

---

## ✅ 기술면접 체크(질문+답)

### Q9. Builder 패턴이 유용한 상황은?

**A.**

- 생성자 인자가 많거나 옵션 조합이 많을 때, 가독성 있게 단계적으로 설정할 수 있어서.

---

# 6) Singleton 패턴

## 핵심

- 앱 전체에서 인스턴스를 하나만 유지
- Kotlin에서는 `object`로 쉽게 구현

```kotlin
object Logger {
    fun d(msg: String) = println(msg)
}
```

---

## 실무 포인트(중요)

- 전역 상태가 커지면 테스트/추적이 어려워짐
- Context를 잡는 싱글톤은 누수 위험
    
    → 가능하면 stateless 유틸 수준으로 사용하거나 DI로 관리
    

---

## ✅ 기술면접 체크(질문+답)

### Q10. Singleton의 장단점은?

**A.**

- 장점: 접근 간편, 전역 공유 용이
- 단점: 전역 상태/결합도 증가, 테스트 어려움, Context 누수 위험

---

# 7) Observer 패턴 (Architecture 연결)

## 핵심

- 데이터 변화를 구독하고 반응하는 구조
- Android에서 LiveData/Flow/Compose가 대표 예시

---

## Android 실무 예제: LiveData observe

```kotlin
vm.name.observe(viewLifecycleOwner) { name ->
    nameText.text = name
}
```

---

## ✅ 기술면접 체크(질문+답)

### Q11. Observer 패턴이 Android에서 왜 중요하죠?

**A.**

- UI가 데이터를 폴링하지 않고 변화를 구독해 자동으로 UI를 갱신하는 구조가 표준이기 때문.

---

# 8) Architecture 연결 정리

- **MVVM/MVP/MVI/Clean**: “전체 구조(큰 설계)”
- **Repository**: “데이터 접근 구조”
- **Observer**: “상태 변화 전달 구조”
- **DI**: “의존성 연결 방식”
- **Factory/Builder**: “객체 생성 방식”
- **Singleton**: “인스턴스 관리 방식”

---
