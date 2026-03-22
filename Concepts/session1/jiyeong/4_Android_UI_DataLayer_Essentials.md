# Android 핵심 컴포넌트 (Context / Intent / ViewModel / LiveData / DataBinding)

---

## 1. Context

### 개념
`Context`는 **앱 환경에 대한 정보를 제공하는 인터페이스**. 리소스 접근, 시스템 서비스 접근, 컴포넌트 시작 등 Android 기능의 대부분에 필요.

> "Context 없이는 Android 앱이 작동하지 않는다"고 할 정도로 핵심적인 존재.

### Context의 종류

#### 1) Application Context (`applicationContext`)
- **앱 전체 수명주기**와 동일하게 유지
- 특정 Activity나 Fragment에 종속되지 않음
- **언제 사용?** 앱 전체에서 사용되는 싱글톤, Repository, 데이터베이스 초기화

```kotlin
// Application Context 사용 예시
val database = Room.databaseBuilder(
    applicationContext,      // Application Context 사용 ✅
    AppDatabase::class.java,
    "app_database"
).build()
```

#### 2) Activity Context (`this` 또는 `activityContext`)
- **Activity의 수명주기**와 동일 (Activity 종료 시 같이 소멸)
- UI 관련 작업에 사용
- **언제 사용?** Dialog 생성, Toast, Adapter, Layout Inflater

```kotlin
// Activity Context 사용 예시
val dialog = AlertDialog.Builder(this) // Activity Context ✅
val toast = Toast.makeText(this, "Hello", Toast.LENGTH_SHORT)
```

#### 3) Service Context
- `Service` 컴포넌트의 Context
- UI 작업에는 부적합

### ⚠️ Context 사용 시 주의사항

| 상황 | 사용할 Context |
|------|---------------|
| 싱글톤 객체에 Context 저장 | `applicationContext` 사용 |
| Dialog / Toast / AlertDialog | `Activity Context` 사용 |
| Room, Retrofit 초기화 | `applicationContext` 사용 |
| RecyclerView Adapter | `Activity Context` 사용 |

> **메모리 릭 주의**: 싱글톤이 Activity Context를 참조하면, Activity가 종료되어도 GC가 수거하지 못해 메모리 릭 발생.

---

## 2. Intent

### 개념
`Intent`는 **컴포넌트 간의 통신 수단**. Activity, Service, BroadcastReceiver를 시작하거나, 데이터를 전달하는 데 사용.

### Intent의 종류

#### 1) Explicit Intent (명시적 인텐트)
- **정확히 어떤 컴포넌트를 실행할지 지정**
- 같은 앱 내에서 Activity 전환에 주로 사용

```kotlin
// 명시적 인텐트: DetailActivity로 이동 + 데이터 전달
val intent = Intent(this, DetailActivity::class.java).apply {
    putExtra("USER_ID", "12345")
    putExtra("USER_NAME", "홍길동")
}
startActivity(intent)

// 받는 쪽 (DetailActivity)
val userId = intent.getStringExtra("USER_ID")
val userName = intent.getStringExtra("USER_NAME")
```

#### 2) Implicit Intent (암시적 인텐트)
- **수행할 액션만 지정**, 어떤 앱/컴포넌트가 처리할지는 시스템이 결정
- 외부 앱과의 연동에 사용

```kotlin
// 암시적 인텐트: 전화 앱 실행
val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:01012345678"))
startActivity(callIntent)

// 암시적 인텐트: 브라우저 열기
val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
startActivity(browserIntent)

// 암시적 인텐트: 이미지 공유
val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, "공유할 텍스트")
}
startActivity(Intent.createChooser(shareIntent, "공유하기"))
```

### Intent Filter
- Manifest에 선언하여 어떤 암시적 인텐트를 수신할지 정의
```xml
<activity android:name=".DetailActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:scheme="https" android:host="myapp.com" />
    </intent-filter>
</activity>
```

### Activity Result API (최신 방식)
```kotlin
// 예전 방식인 startActivityForResult 대신 사용
val launcher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == RESULT_OK) {
        val data = result.data?.getStringExtra("KEY")
    }
}

launcher.launch(Intent(this, SecondActivity::class.java))
```

---

## 3. ViewModel

### 개념
UI 관련 데이터를 **수명주기를 고려하여** 저장하고 관리하는 클래스.  
`AAC(Android Architecture Components)`의 핵심 컴포넌트.

### ViewModel이 필요한 이유
Activity/Fragment는 화면 회전, 언어 변경 등으로 **재생성**됨.  
→ 기존에는 재생성 때마다 데이터가 초기화되는 문제 발생.  
→ ViewModel은 재생성 시에도 **동일 인스턴스가 유지**됨.

```
Activity 생성 → ViewModel 생성
Activity 회전 → Activity 재생성 (ViewModel 유지 ✅)
Activity 종료 → ViewModel.onCleared() 호출 후 소멸
```

### 생명주기

```
         ┌──────────────────────────────────┐
         │           ViewModel Scope         │
  onCreate│                                  │finish()
─────────┤  Activity/Fragment 재생성 반복    ├──────── onCleared()
         │                                  │
         └──────────────────────────────────┘
```

### 기본 사용법
```kotlin
class UserViewModel : ViewModel() {
    // 외부에는 읽기 전용 StateFlow 노출
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchUsers() {
        viewModelScope.launch {  // ViewModel이 clear될 때 자동 취소
            _isLoading.value = true
            _users.value = userRepository.getUsers()
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 리소스 정리
    }
}

// Activity에서
class MainActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()
    // Fragment에서는: by activityViewModels() (Activity와 ViewModel 공유 시)
}
```

### ViewModel에서 Context 사용이 필요할 때
일반 ViewModel은 Context를 갖지 않음. Context가 필요한 경우 `AndroidViewModel` 사용:
```kotlin
class MyViewModel(application: Application) : AndroidViewModel(application) {
    fun getString(): String {
        return getApplication<Application>().getString(R.string.app_name)
    }
}
```
> ⚠️ 가능하면 ViewModel에서 Context 의존성을 피하는 게 좋음. UseCase나 Repository에서 처리하도록 설계 권장.

---

## 4. LiveData

### 개념
**수명주기를 인식하는(Lifecycle-aware)** 관찰 가능한 데이터 홀더.  
Observer가 활성 상태(STARTED, RESUMED)일 때만 알림을 전달함.

### 특징
- Activity/Fragment가 백그라운드 상태일 때는 이벤트 전달 안 함 → **메모리 릭 방지**
- Activity가 파괴되면 자동으로 Observer 제거

### LiveData vs StateFlow

| | LiveData | StateFlow |
|--|----------|-----------|
| 플랫폼 | Android 전용 | Kotlin 순수 (Android 무관) |
| 초기값 | 없어도 됨 | 반드시 필요 |
| 수명주기 인식 | 자동 | `repeatOnLifecycle` 필요 |
| ViewModel에서 권장 | O (기존) | O (최근 권장) |

```kotlin
// LiveData 사용
class UserViewModel : ViewModel() {
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    fun setName(name: String) {
        _userName.value = name          // Main Thread
        // _userName.postValue(name)   // Background Thread에서 사용
    }
}

// 관찰 (Activity)
viewModel.userName.observe(this) { name ->
    binding.tvName.text = name
}
```

```kotlin
// StateFlow 사용 (최신 권장)
class UserViewModel : ViewModel() {
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
}

// 관찰 (Activity) - Lifecycle 처리 필요
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.userName.collect { name ->
            binding.tvName.text = name
        }
    }
}
```

---

## 5. DataBinding

### 개념
XML 레이아웃에서 **UI 컴포넌트와 데이터를 직접 바인딩**하는 라이브러리.  
`findViewById()`나 `binding.view.text = value` 같은 코드를 XML에서 처리 가능.

### 설정
```gradle
// build.gradle (app)
android {
    buildFeatures {
        dataBinding = true
    }
}
```

### 기본 사용법
```xml
<!-- layout을 루트 태그로 감싸야 함 -->
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <data>
        <variable
            name="viewModel"
            type="com.example.UserViewModel" />
    </data>

    <LinearLayout ...>
        <!-- ViewModel의 LiveData를 직접 바인딩 -->
        <TextView
            android:text="@{viewModel.userName}"
            android:visibility="@{viewModel.isLoading ? View.GONE : View.VISIBLE}" />

        <!-- 양방향 바인딩 (@={}) -->
        <EditText
            android:text="@={viewModel.inputText}" />

        <!-- 클릭 이벤트 바인딩 -->
        <Button
            android:onClick="@{() -> viewModel.onButtonClick()}" />
    </LinearLayout>
</layout>
```

```kotlin
// Activity에서 DataBinding 설정
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this  // LiveData 관찰을 위해 필수
        binding.viewModel = viewModel
    }
}
```

### ViewBinding vs DataBinding

| | ViewBinding | DataBinding |
|--|-------------|-------------|
| 목적 | View 참조만 제공 | 데이터와 View 바인딩 |
| XML 수정 | 불필요 | `<layout>` 태그 필요 |
| 빌드 속도 | 빠름 | 느림 (어노테이션 처리) |
| 표현식 지원 | ❌ | ✅ (`@{}`, `@={}`) |
| 권장 상황 | 단순 View 참조 | ViewModel과 양방향 바인딩 |
