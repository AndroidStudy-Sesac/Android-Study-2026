# Android 스터디 1회차 — Part D

## DataBinding · Paging · WorkManager · Navigation

## 전체 핵심 요약

- **DataBinding**: XML과 데이터(보통 ViewModel)를 바인딩해 UI 업데이트 코드를 줄이는 방식
- **Paging**: 대용량 리스트를 “페이지 단위”로 로드하여 메모리/네트워크 부담을 줄이는 Jetpack 라이브러리
- **WorkManager**: “반드시 실행되어야 하는 백그라운드 작업”을 제약조건과 함께 예약/관리하는 표준 도구
- **Navigation**: 화면 이동/Back stack/인자 전달을 graph로 관리하는 Jetpack 표준
- 실무에서는 이 4개가 각각 따로가 아니라, **MVVM(ViewModel) + Repository + Flow/LiveData**와 결합되어 사용됨

---

# 1) DataBinding

## 1-1. 핵심

- XML 레이아웃에서 데이터를 직접 참조하고, UI를 자동 갱신할 수 있게 해줌
- findViewById → setText/setVisibility 같은 반복 코드를 줄이는 것이 목적
- MVVM 구조에서 ViewModel을 뷰에 바인딩하는 형태로 많이 사용

---

## 1-2. 실무 예제 (가장 전형적인 형태)

### (1) layout XML (data binding 활성화 가정)

```xml
<layout>
    <data>
        <variable
            name="vm"
            type="com.example.ProfileViewModel" />
    </data>

    <TextView
        android:id="@+id/nameText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@{vm.name}" />
</layout>
```

> 보통 `vm.name`은 `LiveData<String>`이거나, `ObservableField` 등을 사용합니다.
> 
> 
> (프로젝트 설정/스타일에 따라 구현 방식이 다름)
> 

### (2) Fragment에서 binding 연결

```kotlin
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val vm: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentProfileBinding.bind(view)
        binding.vm = vm
        binding.lifecycleOwner = viewLifecycleOwner // LiveData 자동 갱신을 위해 중요
    }
}
```

---

## 1-3. 실무 포인트

- `binding.lifecycleOwner = viewLifecycleOwner`를 안 넣으면 LiveData 변경이 XML에 반영되지 않는 경우가 많음
- DataBinding은 강력하지만, XML이 복잡해지면 디버깅이 어려워질 수 있음
- 팀/프로젝트에 따라 **ViewBinding + Compose**로 대체되는 경우도 있음(키워드만)

---

## ✅ 기술면접 체크(질문+답)

### Q1. DataBinding을 쓰는 이유는?

**A.**

- UI 업데이트 코드를 줄이고, ViewModel 데이터와 UI를 선언적으로 연결해 유지보수성을 높이기 위해.

### Q2. DataBinding에서 lifecycleOwner를 설정하는 이유는?

**A.**

- LiveData가 lifecycle-aware하게 동작하여 값 변경 시 XML이 자동 갱신되게 하기 위해.

### Q3. DataBinding의 단점/주의점은?

**A.**

- XML 로직이 복잡해지면 디버깅이 어려워지고, 빌드/컴파일 에러가 난해해질 수 있음.

---

# 2) Paging (Paging 3)

## 2-1. 핵심

- 대규모 리스트를 한 번에 다 가져오지 않고 “페이지 단위”로 로딩
- 메모리/네트워크/DB 부하 감소
- RecyclerView와 함께 사용이 매우 흔함
- Flow 기반(`Flow<PagingData<T>>`)으로 ViewModel에서 노출하는 패턴이 표준

---

## 2-2. 실무 예제

### (1) PagingSource (네트워크/DB에서 페이지 로드)

```kotlin
class UserPagingSource(
    private val api: UserApi
) : PagingSource<Int, User>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val page = params.key ?: 1
        return try {
            val response = api.fetchUsers(page = page, size = params.loadSize)
            LoadResult.Page(
                data = response.users,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.users.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, User>): Int? = null
}
```

### (2) Repository → Pager 생성

```kotlin
class UserRepository(private val api: UserApi) {
    fun getUsers(): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { UserPagingSource(api) }
        ).flow
    }
}
```

### (3) ViewModel에서 캐시 + 노출

```kotlin
class UserListViewModel(
    private val repo: UserRepository
) : ViewModel() {

    val users: Flow<PagingData<User>> =
        repo.getUsers().cachedIn(viewModelScope)
}
```

### (4) Fragment에서 수집해 Adapter에 제출

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.users.collectLatest { pagingData ->
        adapter.submitData(pagingData)
    }
}
```

---

## 2-3. 실무 포인트

- `cachedIn(viewModelScope)`는 화면 회전 등에서 paging 결과를 재사용하는 데 도움이 됨
- loadState(로딩/에러)를 UI에 표시하는 패턴이 거의 필수
- Paging은 “무한 스크롤” 구현의 표준 솔루션

---

## ✅ 기술면접 체크(질문+답)

### Q4. Paging을 왜 쓰나요?

**A.**

- 대용량 데이터를 한 번에 로드하지 않고 페이지 단위로 로드해 성능/메모리/네트워크 부담을 줄이기 위해.

### Q5. PagingSource의 역할은?

**A.**

- “특정 페이지를 어떻게 로드할지”를 정의하는 컴포넌트(데이터 로딩 로직의 핵심).

### Q6. cachedIn을 왜 쓰나요?

**A.**

- ViewModel scope 안에서 paging 스트림을 캐시해 재생성(회전) 시 불필요한 재로드를 줄이기 위해.

---

# 3) WorkManager

## 3-1. 핵심

- 앱이 종료되거나 백그라운드에서도 “반드시 실행되어야 하는 작업”을 예약/보장하는 도구
- 제약 조건(네트워크/충전 중/배터리 상태 등)을 걸 수 있음
- Android 백그라운드 제한이 강해지면서 WorkManager가 “표준 선택지”가 됨

---

## 3-2. 언제 쓰나? (실무 사례)

- 로그/분석 데이터 업로드
- 이미지 업로드 재시도
- 주기적인 동기화
- 네트워크 연결될 때만 실행해야 하는 작업
- 앱 강제 종료/재부팅 이후에도 이어져야 하는 작업

---

## 3-3. 실무 예제 (OneTimeWorkRequest + 제약조건)

### (1) Worker 구현

```kotlin
class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 업로드 작업 가정
            upload()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

### (2) 제약조건 + 작업 요청

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

val request = OneTimeWorkRequestBuilder<UploadWorker>()
    .setConstraints(constraints)
    .build()

WorkManager.getInstance(requireContext()).enqueue(request)
```

---

## 3-4. 실무 포인트

- 실패 시 `Result.retry()`로 재시도 정책 적용 가능
- “정확한 시간에 실행”은 WorkManager보단 Alarm 계열이 적합한 경우도 있음(키워드만)
- WorkManager는 “최대한 조건 만족 시 실행” + “실행 보장” 쪽에 강함

---

## ✅ 기술면접 체크(질문+답)

### Q7. WorkManager는 언제 쓰나요?

**A.**

- 앱이 종료/백그라운드여도 반드시 실행되어야 하는 작업(동기화/업로드 등)에 사용.

### Q8. WorkManager와 일반 Coroutine(launch)의 차이는?

**A.**

- 코루틴은 앱 프로세스 내에서만 실행되고 프로세스 죽으면 끝남.
- WorkManager는 시스템이 작업을 관리해 조건/재시도/재부팅 후에도 실행을 보장할 수 있음.

### Q9. CoroutineWorker를 쓰는 이유는?

**A.**

- doWork를 suspend로 작성 가능해 비동기 작업을 자연스럽게 구현할 수 있기 때문.

---

# 4) Navigation (Jetpack Navigation)

## 4-1. 핵심

- 화면 이동을 graph로 정의하고, back stack을 일관되게 관리
- Fragment 이동/인자 전달을 표준화
- Deep link, nested graph 확장도 가능(키워드만)

---

## 4-2. 실무 예제 (navigate + args)

### (1) 이동(기본)

```kotlin
findNavController().navigate(R.id.action_home_to_detail)
```

### (2) Bundle로 인자 전달(기본)

```kotlin
val bundle = Bundle().apply { putLong("user_id", 1L) }
findNavController().navigate(R.id.action_home_to_detail, bundle)
```

### (3) Detail에서 인자 받기

```kotlin
val userId = requireArguments().getLong("user_id")
```

> 실무에선 key 오타 방지를 위해 `const val`/SafeArgs 사용 (키워드만)
> 

---

## 4-3. 실무 포인트

- Navigation은 화면 흐름이 많아질수록 유지보수성이 좋아짐
- back stack을 직접 조작하는 코드가 줄어듦
- fragment transaction 직접 관리 대비 “규칙이 graph로 모이는 장점”

---

## ✅ 기술면접 체크(질문+답)

### Q10. Navigation을 쓰는 이유는?

**A.**

- 화면 이동과 back stack, 인자 전달을 일관되게 관리해 구조 파악과 유지보수가 쉬워지기 때문.

### Q11. FragmentTransaction 직접 vs Navigation 차이는?

**A.**

- 직접 트랜잭션은 자유도가 높지만 규칙이 분산되기 쉬움.
- Navigation은 이동 흐름이 graph에 모여 변경/확장이 편함.

### Q12. Navigation에서 인자 전달 시 실무에서 중요한 점은?

**A.**

- key 오타/타입 불일치가 버그 포인트라, 상수 관리 또는 SafeArgs 같은 방식으로 안전성을 높이는 게 중요.

---
