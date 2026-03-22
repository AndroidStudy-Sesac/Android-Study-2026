# 1. Lifecycle의 개념

Lifecycle: **컴포넌트가 생성되고 화면에 보이고 상호작용하고 사라질 때까지의 상태 변화 흐름**

안드로이드에서는 이 흐름에 맞춰서 처리해야 하는 것들이 있다:

* 데이터와 상태를 언제 준비할 것인지
* UI를 언제 안전하게 다룰 것인지
* 리소스를 언제 정리할 것인지

---

# 2. Activity와 Fragment의 차이점

## Activity의 역할

**화면 전체를 담는 컨테이너**

## Fragment의 역할

**화면 내부를 구성하는 재사용 가능한 UI 단위**

* Activity는 화면의 큰 단위를 관리
* Fragment는 화면을 기능별로 나누어 구성

이렇게 분리하면 가능해지는 것들:

* 화면 재사용
* 기능 단위 분리
* 유지보수 편의성 향상
* 태블릿이나 멀티 패널 UI 구성이 용이함

---

# 3. Activity Lifecycle 전체 흐름과 역할

## 전체 흐름

```text
onCreate → onStart → onResume → onPause → onStop → onDestroy
                         ↑               ↓
                     onRestart ←────────┘ (재방문 시)
```

## 역할 정리

| 메서드 | 역할 | 주로 하는 일 |
| --- | --- | --- |
| `onCreate` | Activity 생성 및 초기화 | 레이아웃 설정, ViewBinding 초기화, ViewModel 연결 |
| `onStart` | 화면이 사용자에게 보이기 시작 | 화면 표시 직전 준비 |
| `onResume` | 사용자와 상호작용 가능 | 클릭, 입력, 애니메이션 시작 |
| `onPause` | 상호작용 중단 | 임시 저장, 애니메이션 중지 |
| `onStop` | 화면이 완전히 안 보임 | 리소스 해제, 작업 중단 |
| `onRestart` | onStop 이후 다시 화면으로 돌아올 때 | 중단된 작업 재개 준비 |
| `onDestroy` | Activity 제거 | 최종 정리 |
---

# 4. Activity 예시 코드

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate 호출")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleTextView.text = "MainActivity 생성 완료"

        binding.nextButton.setOnClickListener {
            Log.d("MainActivity", "버튼 클릭")
        }

        viewModel.title.observe(this) { title ->
            binding.titleTextView.text = title
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart 호출")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume 호출")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause 호출")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop 호출")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy 호출")
    }
}
```

---

# 5. Fragment Lifecycle 전체 흐름과 역할

## 전체 흐름

```text
onAttach
→ onCreate
→ onCreateView
→ onViewCreated
→ onViewStateRestored
→ onStart
→ onResume
→ onPause
→ onStop
→ onDestroyView
→ onDestroy
→ onDetach
```

이 흐름은 **모두 Fragment의 lifecycle 메서드**이지만, 이 중에서

* `onCreateView`
* `onViewCreated`
* `onViewStateRestored`
* `onDestroyView`

이 네 메서드는 **Fragment 내부의 View를 생성하고 사용하고 제거하는 역할**을 담당

**전부 Fragment lifecycle 메서드이지만, 그중 일부가 View 구간을 담당함**

---

# 6. Fragment Lifecycle 역할 정리

| 메서드 | 대상 | 역할 | 주로 하는 일 |
| --- | --- | --- | --- |
| `onAttach` | Fragment | Activity와 연결 | Context가 필요한 초기 작업 |
| `onCreate` | Fragment | 데이터와 상태 준비 | ViewModel, 인자 처리 |
| `onCreateView` | View | View 생성 | Binding inflate |
| `onViewCreated` | View | UI 초기화 | 클릭 이벤트, observer 등록 |
| `onViewStateRestored` | View | View 상태 복원 완료 | savedInstanceState 기반 UI 복원 |
| `onStart` | Fragment | 화면 표시 시작 (View는 이미 존재) | 화면 표시 직전 반영 |
| `onResume` | Fragment | 상호작용 가능 (View는 이미 존재) | 입력, 클릭 가능 상태 |
| `onPause` | Fragment | 상호작용 중단 | 애니메이션 일시정지 |
| `onStop` | Fragment | 화면 비표시 | 작업 중단 |
| `onDestroyView` | View | View 제거 | binding 정리 |
| `onDestroy` | Fragment | Fragment 제거 | 객체 정리 |
| `onDetach` | Fragment | Activity 연결 해제 | Context 참조 정리 |
---

# 7. 가장 중요한 구조

Fragment는 `onAttach`부터 `onDetach`까지 살아있고, 그 중에서 View는 `onCreateView`에서 생성되고 `onDestroyView`에서 제거된다.

```text
Fragment는 더 오래 살아있을 수 있다.
View는 그보다 짧게 살아있는다.

데이터와 상태는 Fragment 기준으로,
UI는 View가 살아있는 구간 기준으로 생각하기
```

---

# 8. Fragment 전체 예시 코드

```kotlin
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding
            ?: throw IllegalStateException("binding이 null입니다. onCreateView와 onDestroyView 사이에서만 접근해야 합니다.")

    private val viewModel: HomeViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("HomeFragment", "onAttach 호출")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HomeFragment", "onCreate 호출")

        val userId = arguments?.getLong(ARG_USER_ID) ?: -1L
        viewModel.loadUser(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HomeFragment", "onCreateView 호출")

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "onViewCreated 호출")

        binding.titleTextView.text = "초기 화면"

        binding.refreshButton.setOnClickListener {
            viewModel.refreshUser()
        }

        viewModel.userName.observe(viewLifecycleOwner) { userName ->
            binding.titleTextView.text = userName
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d("HomeFragment", "onViewStateRestored 호출")
    }
    
    override fun onStart() {
        super.onStart()
        Log.d("HomeFragment", "onStart 호출")
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeFragment", "onResume 호출")
    }

    override fun onPause() {
        super.onPause()
        Log.d("HomeFragment", "onPause 호출")
    }

    override fun onStop() {
        super.onStop()
        Log.d("HomeFragment", "onStop 호출")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "onDestroyView 호출")

        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HomeFragment", "onDestroy 호출")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("HomeFragment", "onDetach 호출")
    }

    companion object {
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(userId: Long): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USER_ID, userId)
                }
            }
        }
    }
}
```

---

# 9. Fragment 기준 작업과 View 기준 작업을 정확히 구분하기

## Fragment 기준 작업

Fragment 자체가 살아있는 동안 유지되어야 하는 작업

* ViewModel 준비
* arguments 읽기
* 데이터 로딩 시작
* 비UI 로직 처리

이 작업은 주로 `onCreate`에서 함

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val userId = arguments?.getLong(ARG_USER_ID) ?: -1L
    viewModel.loadUser(userId)
}
```

이 코드는 View가 없어도, 즉 UI가 없어도 수행할 수 있는 작업이므로 Fragment 기준 작업이다.

---

## View 기준 작업

View가 실제로 존재할 때만 가능한 작업
* ViewBinding 사용
* 클릭 이벤트 연결
* RecyclerView adapter 연결
* observer 등록 후 UI 반영

이 작업은 주로 `onViewCreated`에서 함

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.refreshButton.setOnClickListener {
        viewModel.refreshUser()
    }

    viewModel.userName.observe(viewLifecycleOwner) { userName ->
        binding.titleTextView.text = userName
    }
}
```

이 코드는 View가 반드시 있어야 하니, View 기준 작업임

---

# 10. `viewLifecycleOwner`를 써야 하는 이유

Fragment는 살아있는데 View는 이미 제거된 상태인 경우, UI observer를 Fragment 자신(`this`)에 묶으면 문제가 생길 수 있기 때문

## 잘못된 예시

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.userName.observe(this) { userName ->
        binding.titleTextView.text = userName
    }
}
```

이 방식은 Fragment 객체 기준으로 observer가 살아남을 수 있다.
그런데 View는 이미 `onDestroyView` 이후 사라졌을 수 있다.
그 상태에서 `binding`에 접근하면 오류나 메모리 누수가 발생할 수 있다.

## 올바른 예시

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.userName.observe(viewLifecycleOwner) { userName ->
        binding.titleTextView.text = userName
    }
}
```

이 방식은 View가 제거될 때 observer도 같이 정리된다.

---

# 11.`onDestroyView`에서 binding을 null 처리해야 하는 이유

Fragment는 아직 살아있는데 View만 제거될 수 있기 때문

따라서 View를 참조하는 binding을 계속 들고 있으면 메모리 누수가 발생할 수 있다.

```kotlin
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

**View가 사라지는 시점에 View 참조를 끊는 작업**을 필수로 해야 함

---

# 12. ViewModel 예시 코드

```kotlin
class HomeViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadUser(userId: Long) {
        _loading.value = true

        val loadedName = if (userId == -1L) {
            "알 수 없는 사용자"
        } else {
            "사용자 ID: $userId"
        }

        _userName.value = loadedName
        _loading.value = false
    }

    fun refreshUser() {
        _loading.value = true
        _userName.value = "새로고침된 사용자 정보"
        _loading.value = false
    }
}
```

---

# 13. 데이터 보존 방법

Lifecycle과 데이터 보존은 유지 범위가 다르다.

| 방법 | 유지 범위 | 설명 |
| --- | --- | --- |
| `ViewModel` | Activity / Fragment 재생성까지 | 화면 회전 시 유지 |
| `savedInstanceState` | 프로세스 종료 후 일부 복원 | 단순 상태 저장 |
| `Room / DB` | 영구 저장 | 앱 종료 후에도 유지 |
| `rememberSaveable` | 프로세스 종료 후 일부 복원 (savedInstanceState와 동일) | Compose에서 savedInstanceState를 대체하는 방식 |
---

# 14. Activity의 View와 Fragment의 View

## Activity의 View가 생성되는 때

Activity는 기본적으로 View를 자동으로 가지지 않는다.
개발자가 명시적으로 레이아웃을 지정해야 View가 생성되고 연결된다.

일반적으로는 `onCreate`에서 `setContentView()`를 호출하는 방식을 사용한다.
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
}
```

또는 생성자에 레이아웃 ID를 직접 넘기는 방식도 있다.
```kotlin
class MainActivity : AppCompatActivity(R.layout.activity_main) { ... }
```

두 방식 모두 결과는 동일하며, 이 시점 이후에 Activity가 갖게 되는 것들:
* 화면을 구성하는 루트 View
* UI를 조작할 수 있는 구조

## Fragment와의 구조 관계

Activity에 Fragment를 사용하는 경우 전체 구조
```text
Activity
 └── Activity의 레이아웃(View)
      └── FragmentContainerView
           └── Fragment의 View
```

---

Activity의 View는 전체 레이아웃이고,

FragmentContainerView는 그 레이아웃의 일부이고,

Fragment의 View는 컨테이너 안에 추가된다

그러므로 Fragment는 Activity의 View 내부에 포함된다

그리고 Fragment의 View는 FragmentContainerView 안에 삽입된다

---

# 15. Activity와 Fragment의 View 생명주기 차이

---

## Activity

* `setContentView()` 이후 View를 사용한다
* 일반적인 UI Activity에서는 View를 계속 유지하면서 사용한다

---

## Fragment

Fragment는 View의 생성과 제거가 분리되어 있다.

```text
onCreate      → Fragment 존재 (View 없음)
onCreateView  → View 생성
onDestroyView → View 제거
```

```text
Fragment 객체는 살아있지만
View는 없는 상태가 가능하다
```

이 차이 때문에 
* UI를 다루는 시점
* 상태를 복원하는 방식

이 달라진다.

---

# 16. savedInstanceState 사용 방식

## Activity

Activity에서는 일반적으로 View가 설정된 이후에
상태 복원과 UI 반영을 함께 처리한다.

```kotlin
class CounterActivity : AppCompatActivity() {

    private var count = 0
    private lateinit var binding: ActivityCounterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태 복원
        count = savedInstanceState?.getInt("count") ?: 0

        // UI 반영
        binding.textView.text = count.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("count", count)
    }
}
```

---

## Fragment

Fragment는 View가 없을 수 있기 때문에
데이터 복원과 UI 반영을 분리해야 한다.

---

### 데이터 복원 (Fragment 기준)

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    count = savedInstanceState?.getInt("count") ?: 0
}
```

---

### UI 반영 (View 기준)

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.textView.text = count.toString()
}
```

---

# 17. 가장 중요한 차이

| 구분      | Activity                          | Fragment      |
|----------|-----------------------------------|--------------|
| View 생성 | setContentView                   | onCreateView |
| View 제거 | 별도 View 제거 단계 없음            | onDestroyView |
| UI 접근   | 비교적 단순                        | View 존재 시점 필요 |
---
