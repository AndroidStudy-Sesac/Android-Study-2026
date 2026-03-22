# 아키텍처 패턴 정리 (MVC / MVP / MVVM / MVI / Clean Architecture)

---

## 1. MVC (Model - View - Controller)

### 개념
UI와 비즈니스 로직을 분리하기 위한 가장 고전적인 패턴.

| 구성 요소 | 역할 |
|-----------|------|
| **Model** | 데이터와 비즈니스 로직 담당 |
| **View** | UI 렌더링 담당 (사용자에게 보이는 화면) |
| **Controller** | 사용자 입력을 받아 Model과 View를 연결 |

### 흐름
```
User Input → Controller → Model → View → 사용자에게 표시
```

### Android에서의 문제점
- Android에서는 Activity/Fragment가 Controller 역할을 하면서 **View 역할도 겸하게 됨**
- 결과적으로 Activity가 비대해져 **Massive Activity** 문제 발생
- View와 Controller 간 결합도가 높아 테스트가 어려움

---

## 2. MVP (Model - View - Presenter)

### 개념
MVC의 단점을 보완한 패턴. Controller 대신 **Presenter**를 사용해 View와 Model을 완전히 분리.

| 구성 요소 | 역할 |
|-----------|------|
| **Model** | 데이터와 비즈니스 로직 |
| **View** | UI 렌더링, Presenter에게 이벤트 전달. Interface로 정의 |
| **Presenter** | View와 Model 사이의 중간자. UI 로직 처리 |

### 흐름
```
User Input → View → Presenter → Model
                  ← Presenter ←
```

### 특징
- View는 Interface로 추상화되므로 **단위 테스트**가 용이
- View와 Presenter가 **1:1 관계**로 강하게 결합됨
- View Interface를 많이 만들어야 해서 **보일러플레이트 코드** 증가

---

## 3. MVVM (Model - View - ViewModel)

### 개념
Android 공식 권장 패턴. View와 비즈니스 로직을 **데이터 바인딩/옵저버 패턴**으로 분리.

| 구성 요소 | 역할 |
|-----------|------|
| **Model** | 데이터와 비즈니스 로직 (Repository 포함) |
| **View** | UI 렌더링. ViewModel을 관찰(Observe) |
| **ViewModel** | View에 필요한 데이터를 가공·보유. View에 대한 직접 참조 없음 |

### 흐름
```
User Input → View → ViewModel → Model(Repository)
View ← (LiveData/StateFlow 관찰) ← ViewModel ←
```

### ViewModel의 핵심 특성
- **생명주기 독립**: Activity/Fragment가 재생성(회전 등)되어도 ViewModel은 유지됨
- **View 참조 없음**: Context나 View를 직접 참조하지 않아 메모리 릭 방지
- `ViewModelProvider`로 생성하며, 소유자(Activity/Fragment)가 파괴될 때 `onCleared()` 호출

```kotlin
class MainViewModel : ViewModel() {
    private val _uiState = MutableLiveData<String>()
    val uiState: LiveData<String> = _uiState

    fun loadData() {
        _uiState.value = "Hello MVVM"
    }
}

// Activity에서
val viewModel: MainViewModel by viewModels()
viewModel.uiState.observe(this) { state ->
    binding.textView.text = state
}
```

### MVP vs MVVM 비교
| | MVP | MVVM |
|--|-----|------|
| View-Logic 결합 | Presenter가 View 직접 참조 | ViewModel은 View 모름 |
| 테스트 | View Interface 필요 | ViewModel만 단독 테스트 가능 |
| 보일러플레이트 | 많음 | 적음 (DataBinding/Flow 활용) |

---

## 4. MVI (Model - View - Intent)

### 개념
**단방향 데이터 흐름(UDF, Unidirectional Data Flow)** 을 강제하는 패턴.  
Compose 환경에서 특히 잘 어울리며, 상태 관리를 예측 가능하게 만듦.

| 구성 요소 | 역할 |
|-----------|------|
| **Model** | UI State (불변 데이터 클래스로 표현) |
| **View** | State를 받아 렌더링. Intent를 ViewModel로 전달 |
| **Intent** | 사용자 행동/이벤트를 나타내는 sealed class |

> ⚠️ 여기서 **Intent**는 Android의 `android.content.Intent`가 아닌, 사용자의 **의도(action)** 를 의미하는 개념적 용어입니다.

### 흐름 (단방향)
```
View → Intent(Action) → ViewModel → 새로운 State → View 갱신
```

### 핵심: 불변 State
```kotlin
// State: 화면의 모든 상태를 단일 데이터 클래스로 표현
data class MainUiState(
    val isLoading: Boolean = false,
    val items: List<String> = emptyList(),
    val error: String? = null
)

// Intent: 가능한 모든 사용자 행동
sealed class MainIntent {
    object LoadData : MainIntent()
    data class DeleteItem(val id: String) : MainIntent()
}

// ViewModel
class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.LoadData -> loadData()
            is MainIntent.DeleteItem -> deleteItem(intent.id)
        }
    }
}
```

### MVI의 장점
- State가 불변이라 디버깅 및 테스트가 쉬움
- 단방향 흐름으로 상태 변화 추적이 명확
- Compose의 상태 관리 철학과 자연스럽게 결합

---

## 5. Clean Architecture

### 개념
**Robert C. Martin(Uncle Bob)** 이 제안한 아키텍처. 코드를 계층으로 나눠 **의존성 방향을 안쪽(도메인)으로만** 향하게 강제함.

### 3계층 구조 (Android 맥락)

```
┌─────────────────────────────────────┐
│         Presentation Layer          │  ← UI, ViewModel
│   (Activity, Fragment, Compose)     │
├─────────────────────────────────────┤
│           Domain Layer              │  ← 핵심 비즈니스 로직
│   (UseCase, Entity, Repository      │
│         Interface)                  │
├─────────────────────────────────────┤
│            Data Layer               │  ← 데이터 소스
│   (Repository 구현체, API, DB)       │
└─────────────────────────────────────┘
```

### 의존성 규칙 (Dependency Rule)
> **바깥 계층은 안쪽 계층에 의존할 수 있지만, 안쪽 계층은 바깥을 알 수 없다.**

```
Presentation → Domain ← Data
```
- Domain Layer는 Android Framework를 import하지 않음 (순수 Kotlin)
- 따라서 Domain Layer는 단독으로 JVM 단위 테스트 가능

### 각 계층 상세

#### Domain Layer (가장 안쪽, 가장 중요)
```kotlin
// Entity: 핵심 비즈니스 객체
data class User(val id: String, val name: String)

// Repository Interface: 데이터 접근 추상화 (구현은 Data Layer)
interface UserRepository {
    suspend fun getUser(id: String): User
}

// UseCase: 단일 비즈니스 로직 단위
class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String): User {
        return repository.getUser(id)
    }
}
```

#### Data Layer
```kotlin
// Repository 구현체
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource
) : UserRepository {
    override suspend fun getUser(id: String): User {
        return remoteDataSource.fetchUser(id)
    }
}
```

#### Presentation Layer
```kotlin
class UserViewModel(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    fun loadUser(id: String) {
        viewModelScope.launch {
            val user = getUserUseCase(id)
            // UI 상태 업데이트
        }
    }
}
```

### Clean Architecture + MVVM 조합 (Android 실무 패턴)
실무에서는 Clean Architecture의 계층 구조에 MVVM 패턴을 Presentation Layer에 적용하는 방식이 일반적입니다.

```
Presentation(MVVM) / Domain(UseCase, Entity) / Data(Repository, API, DB)
```

### 장점 요약
| 장점 | 설명 |
|------|------|
| 테스트 용이성 | 각 계층을 독립적으로 테스트 가능 |
| 유지보수성 | 변경 사항이 한 계층에만 영향 |
| 확장성 | 새 기능 추가 시 기존 코드 수정 최소화 |
| 독립성 | UI, DB, Framework 교체에 유연 |
