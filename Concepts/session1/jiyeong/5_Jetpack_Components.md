# Android Jetpack 컴포넌트 (Paging / WorkManager / Navigation)

---

## 1. Paging 3

### 개념
대량의 데이터를 **페이지 단위로 나눠서** 효율적으로 로드하는 Jetpack 라이브러리.  
전체 데이터를 한 번에 불러오지 않고, 사용자가 스크롤할 때 필요한 만큼만 로드.

### 왜 필요한가?
- 수천 개의 아이템을 한 번에 불러오면 → 메모리 낭비, 느린 초기 로딩, 앱 크래시
- Paging은 네트워크와 로컬 DB를 연동한 자동 페이지 로드 처리

### 구성 요소

| 구성 요소 | 역할 |
|-----------|------|
| `PagingSource` | 데이터 로드 로직 정의 (API 또는 DB) |
| `Pager` | PagingSource를 Flow로 변환 |
| `PagingData` | 페이지 데이터를 감싸는 컨테이너 |
| `PagingDataAdapter` | RecyclerView에 PagingData 연결 |
| `RemoteMediator` | 네트워크 + 로컬 DB 조합 시 사용 |

### 기본 흐름
```
API/DB → PagingSource → Pager → Flow<PagingData<T>> → ViewModel → UI(PagingDataAdapter)
```

### 구현 예시
```kotlin
// 1. PagingSource 구현
class NewsApiPagingSource(
    private val apiService: NewsApiService
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1  // 첫 페이지는 1
            val response = apiService.getArticles(page = page, size = params.loadSize)

            LoadResult.Page(
                data = response.articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.articles.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition
    }
}

// 2. Repository에서 Pager 생성
class NewsRepository(private val apiService: NewsApiService) {
    fun getArticles(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,         // 한 번에 로드할 아이템 수
                prefetchDistance = 5,  // 미리 로드를 시작할 남은 아이템 수
                enablePlaceholders = false
            ),
            pagingSourceFactory = { NewsApiPagingSource(apiService) }
        ).flow
    }
}

// 3. ViewModel
class NewsViewModel(private val repository: NewsRepository) : ViewModel() {
    val articles: Flow<PagingData<Article>> = repository.getArticles()
        .cachedIn(viewModelScope) // 화면 회전 시 데이터 유지
}

// 4. PagingDataAdapter
class ArticleAdapter : PagingDataAdapter<Article, ArticleViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(...) = ArticleViewHolder(...)
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position) // null 가능 (placeholder)
        article?.let { holder.bind(it) }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(old: Article, new: Article) = old.id == new.id
            override fun areContentsTheSame(old: Article, new: Article) = old == new
        }
    }
}

// 5. Fragment에서 관찰
lifecycleScope.launch {
    viewModel.articles.collectLatest { pagingData ->
        adapter.submitData(pagingData)
    }
}

// 로딩 상태 처리
adapter.addLoadStateListener { loadState ->
    binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
    binding.retryButton.isVisible = loadState.refresh is LoadState.Error
}
```

---

## 2. WorkManager

### 개념
**보장된 백그라운드 작업** 실행을 위한 Jetpack 라이브러리.  
앱이 종료되거나 기기가 재시작되어도 작업이 실행되는 것을 보장.

### 언제 사용?
- 네트워크 업로드/다운로드 (즉시 하지 않아도 되는 작업)
- 주기적인 데이터 동기화
- 로그 업로드, 이미지 압축 처리
- 백업 작업

### WorkManager vs 다른 백그라운드 처리

| | WorkManager | Coroutine(suspend) | AlarmManager |
|--|-------------|-------------------|--------------|
| 보장 여부 | ✅ 앱 종료 후에도 실행 | ❌ 프로세스 종료 시 취소 | △ Doze Mode 영향 |
| 적합한 작업 | 지연 가능한 중요 작업 | 즉시 필요한 작업 | 정확한 시간 알람 |

### 기본 사용
```kotlin
// 1. Worker 구현
class UploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val fileUri = inputData.getString("FILE_URI") ?: return Result.failure()
            uploadFile(fileUri)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()  // 최대 3번 재시도
            else Result.failure()
        }
    }
}

// 2. 작업 요청 생성
val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
    .setInputData(workDataOf("FILE_URI" to fileUri))
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 네트워크 연결 시에만
            .setRequiresBatteryNotLow(true)               // 배터리 충분할 때만
            .build()
    )
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES) // 재시도 정책
    .build()

// 3. WorkManager에 작업 제출
WorkManager.getInstance(context).enqueue(uploadRequest)

// 4. 주기적 작업 (최소 15분 간격)
val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval = 1,
    repeatIntervalTimeUnit = TimeUnit.HOURS
).build()

WorkManager.getInstance(context)
    .enqueueUniquePeriodicWork(
        "sync_work",
        ExistingPeriodicWorkPolicy.KEEP, // 이미 있으면 유지
        periodicRequest
    )

// 5. 작업 상태 관찰
WorkManager.getInstance(context)
    .getWorkInfoByIdLiveData(uploadRequest.id)
    .observe(this) { workInfo ->
        when (workInfo.state) {
            WorkInfo.State.RUNNING -> showProgress()
            WorkInfo.State.SUCCEEDED -> showSuccess()
            WorkInfo.State.FAILED -> showError()
            else -> {}
        }
    }
```

### 작업 체이닝
```kotlin
// 순차 실행: A → B → C
WorkManager.getInstance(context)
    .beginWith(workA)
    .then(workB)
    .then(workC)
    .enqueue()

// 병렬 → 순차: (A, B) 완료 후 C
WorkManager.getInstance(context)
    .beginWith(listOf(workA, workB))
    .then(workC)
    .enqueue()
```

---

## 3. Navigation Component

### 개념
앱 내 **화면 전환(내비게이션)을 시각적으로 관리**하는 Jetpack 라이브러리.  
Fragment 간 이동, Back Stack 관리, Deep Link 처리를 일관되게 처리.

### 구성 요소

| 구성 요소 | 역할 |
|-----------|------|
| **Navigation Graph** | 화면(목적지)과 이동 경로를 정의한 XML |
| **NavHost** | Navigation Graph를 표시하는 컨테이너 (Fragment) |
| **NavController** | 실제 화면 전환을 수행하는 컨트롤러 |

### 설정
```gradle
dependencies {
    implementation "androidx.navigation:navigation-fragment-ktx:2.7.x"
    implementation "androidx.navigation:navigation-ui-ktx:2.7.x"
}
```

### Navigation Graph (nav_graph.xml)
```xml
<!-- res/navigation/nav_graph.xml -->
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    app:startDestination="@id/homeFragment">

    <fragment
        android:id="@+id/homeFragment"
        android:name="com.example.HomeFragment">
        <action
            android:id="@+id/action_home_to_detail"
            app:destination="@id/detailFragment">
            <!-- 화면 전환 애니메이션 -->
            <app:enterAnim="@anim/slide_in_right"
            <app:exitAnim="@anim/slide_out_left" />
        </action>
    </fragment>

    <fragment
        android:id="@+id/detailFragment"
        android:name="com.example.DetailFragment">
        <!-- Safe Args로 전달할 인자 정의 -->
        <argument
            android:name="userId"
            app:argType="string" />
    </fragment>
</navigation>
```

### Activity 레이아웃 (NavHost 설정)
```xml
<FragmentContainerView
    android:id="@+id/nav_host_fragment"
    android:name="androidx.navigation.fragment.NavHostFragment"
    app:navGraph="@navigation/nav_graph"
    app:defaultNavHost="true" />  <!-- 시스템 Back 버튼 처리 -->
```

### Fragment에서 화면 전환
```kotlin
// 일반 이동
findNavController().navigate(R.id.action_home_to_detail)

// Safe Args로 데이터 전달 (타입 안전)
val action = HomeFragmentDirections.actionHomeToDetail(userId = "123")
findNavController().navigate(action)

// 받는 쪽
val args: DetailFragmentArgs by navArgs()
val userId = args.userId

// 뒤로 가기
findNavController().navigateUp()

// 특정 목적지로 돌아가기 (Back Stack 정리)
findNavController().popBackStack(R.id.homeFragment, inclusive = false)
```

### BottomNavigationView 연결
```kotlin
val navController = findNavController(R.id.nav_host_fragment)
binding.bottomNav.setupWithNavController(navController)
```

### Deep Link 처리
```xml
<!-- Navigation Graph에 Deep Link 추가 -->
<fragment android:id="@+id/detailFragment">
    <deepLink app:uri="myapp://detail/{userId}" />
</fragment>
```

```xml
<!-- AndroidManifest.xml -->
<activity android:name=".MainActivity">
    <nav-graph android:value="@navigation/nav_graph" />
</activity>
```
