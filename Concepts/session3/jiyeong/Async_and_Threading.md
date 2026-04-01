# Async & Threading

---

## 1. Cold Stream vs Hot Stream

| 구분            | Cold Stream          | Hot Stream                           |
|---------------|----------------------|--------------------------------------|
| **데이터 생산 시점** | 구독자가 생길 때마다 새로 시작    | 구독자 유무와 상관없이 독립적으로 흐름                |
| **구독자 수**     | 1:1 (unicast)        | 1:N (multicast)                      |
| **대표 예시**     | `Flow`, `Observable` | `SharedFlow`, `StateFlow`, `Subject` |
| **구독 시 데이터**  | 처음부터 받음              | 구독 이후 데이터부터 받음                       |

### Cold Stream

```kotlin
val coldFlow = flow {
    println("데이터 생산 시작") // 구독할 때마다 실행됨
    emit(1)
    emit(2)
    emit(3)
}

// 각각의 collect가 독립적인 스트림을 생성
coldFlow.collect { println("구독자A: $it") }
coldFlow.collect { println("구독자B: $it") }
```

- 각 `collect` 호출마다 **람다 블록이 새로 실행**됨
- 구독자가 없으면 데이터를 생산하지 않으므로 **리소스 낭비 없음**
- 주로 **네트워크 요청, DB 쿼리** 등 1회성 작업에 적합

### Hot Stream

```kotlin
val hotFlow = MutableSharedFlow<Int>()

// 구독자가 없어도 데이터를 emit할 수 있음
hotFlow.emit(1) // 아무도 못 받음

// 이후에 구독해도 이전 데이터는 받지 못함
hotFlow.collect { println("구독자A: $it") }
```

- 스트림이 **공유**되므로 여러 구독자가 동일한 데이터를 받음
- 구독 이전에 발생한 데이터는 기본적으로 수신 불가 (replay 설정에 따라 다름)
- 주로 **이벤트 버스, UI 상태 관리**에 적합

### 면접 대비 모범 답안

> "Flow는 cold stream이기 때문에 collect가 호출될 때마다 새로운 데이터 스트림이 시작됩니다. 반면 SharedFlow/StateFlow는 hot stream으로, 여러 구독자가 동일한 스트림을
> 공유합니다. ViewModel에서 UI 이벤트를 SharedFlow로, UI 상태를 StateFlow로 관리하는 이유가 바로 이 특성 때문입니다."

---

## 2. Flow / SharedFlow / StateFlow

### Flow (Cold Stream)

```kotlin
// 기본 Flow 생성
fun fetchData(): Flow<String> = flow {
    delay(1000) // 비동기 작업
    emit("데이터1")
    delay(1000)
    emit("데이터2")
}

// ViewModel에서 사용
viewModelScope.launch {
    fetchData()
        .catch { e -> /* 에러 처리 */ }
        .collect { data ->
            _uiState.value = data
        }
}
```

**주요 특징**

- **suspend 함수와 조합** 가능
- `map`, `filter`, `combine`, `zip` 등 다양한 연산자 지원
- `catch`, `onCompletion` 으로 에러 및 완료 처리
- **collect하는 동안만 동작** (취소 시 자동 정리)

### SharedFlow (Hot Stream)

```kotlin
class MyViewModel : ViewModel() {
    // replay=0: 이전 데이터 재전송 없음 (이벤트성 데이터에 적합)
    private val _event = MutableSharedFlow<UiEvent>(replay = 0)
    val event: SharedFlow<UiEvent> = _event.asSharedFlow()

    fun onButtonClick() {
        viewModelScope.launch {
            _event.emit(UiEvent.NavigateToDetail)
        }
    }
}

// Compose에서 수집
LaunchedEffect(Unit) {
    viewModel.event.collect { event ->
        when (event) {
            is UiEvent.NavigateToDetail -> navController.navigate("detail")
        }
    }
}
```

- `replay`: 새 구독자에게 이전 n개 데이터 재전송 여부
- `extraBufferCapacity`: 버퍼 크기 설정
- **일회성 이벤트** (토스트, 네비게이션)에 적합
- 구독자가 없어도 `emit` 가능

### StateFlow (Hot Stream)

```kotlin
class MyViewModel : ViewModel() {
    // 초기값 필수
    private val _uiState = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val data = repository.getData()
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message)
            }
        }
    }
}

// Compose에서 수집
val uiState by viewModel.uiState.collectAsState()
```

- **항상 최신 값을 가짐** (초기값 필수)
- `replay = 1`인 SharedFlow와 유사하나, **동일한 값은 emit하지 않음** (distinctUntilChanged)
- `value` 프로퍼티로 현재 값 즉시 접근 가능
- **UI 상태 관리**에 최적화

### Flow vs SharedFlow vs StateFlow

| 구분         | Flow       | SharedFlow | StateFlow    |
|------------|------------|------------|--------------|
| **스트림 종류** | Cold       | Hot        | Hot          |
| **초기값**    | 없음         | 없음         | 필수           |
| **현재값 접근** | 불가         | 불가         | `.value`로 가능 |
| **중복값 처리** | 모두 emit    | 모두 emit    | 중복 무시        |
| **주요 용도**  | 1회성 비동기 작업 | 이벤트 전달     | UI 상태 관리     |

### 면접 대비 모범 답안

> "SharedFlow는 이벤트성 데이터(토스트 메시지, 화면 이동 등)에 사용하고, StateFlow는 UI 상태처럼 항상 현재 값이 필요한 경우에 사용합니다. LiveData와 달리 StateFlow는 코루틴
> 기반이며 구성 변경(Configuration Change)에도 별도 처리 없이 안전합니다."

---

## 3. 코루틴 vs 스레드

### 스레드 (Thread)

```kotlin
// 전통적인 스레드 사용
Thread {
    // 백그라운드 작업
    val data = fetchDataFromNetwork() // 블로킹 호출

    // UI 업데이트는 반드시 메인 스레드에서
    runOnUiThread {
        textView.text = data
    }
}.start()
```

**스레드의 문제점**

- 스레드 생성 비용이 **큼** (약 1~2MB 스택 메모리)
- 스레드 전환(Context Switch) 비용 발생
- **콜백 지옥** 발생 가능
- 취소 처리가 복잡함

### 코루틴 (Coroutine)

```kotlin
// 코루틴 사용
viewModelScope.launch {
    // 마치 동기 코드처럼 작성 가능
    val data = withContext(Dispatchers.IO) {
        fetchDataFromNetwork() // 스레드를 블로킹하지 않음
    }
    // 자동으로 메인 스레드로 전환
    textView.text = data
}
```

**코루틴의 장점**

- **경량**: 수천 개의 코루틴을 단일 스레드에서 실행 가능
- **구조화된 동시성(Structured Concurrency)**: 부모-자식 관계로 생명주기 관리
- **취소 처리 용이**: `job.cancel()`로 간단히 취소
- 동기 코드처럼 작성 가능 → **가독성 향상**

### 차이점

| 구분        | Thread                 | Coroutine                            |
|-----------|------------------------|--------------------------------------|
| **단위**    | OS 수준의 실행 단위           | 언어 수준의 경량 실행 단위                      |
| **메모리**   | ~1MB 스택                | 수 KB                                 |
| **블로킹**   | 스레드 블로킹                | suspend로 중단 (스레드 해제)                 |
| **전환 비용** | OS Context Switch (비쌈) | 코루틴 Switch (저렴)                      |
| **취소**    | 복잡 (interrupt)         | 간단 (cancel)                          |
| **에러 처리** | try-catch, 복잡          | try-catch, CoroutineExceptionHandler |

### suspend 함수 동작 원리

```kotlin
suspend fun fetchData(): String {
    delay(1000) // 스레드를 블로킹하지 않고 중단(suspend)
    return "데이터"
}
```

- `delay()` 호출 시 **코루틴이 일시 중단**되고 스레드는 다른 코루틴을 실행
- `delay()` 완료 후 **코루틴이 재개**됨
- 스레드 자원을 점유하지 않으므로 효율적

### 면접 대비 모범 답안

> "코루틴은 스레드 위에서 동작하지만 스레드 자체는 아닙니다. suspend 함수가 중단되는 동안 해당 스레드는 다른 작업을 처리할 수 있어 적은 스레드로 많은 동시 작업이 가능합니다. 또한 ViewModel의
> viewModelScope와 결합하면 ViewModel이 클리어될 때 자동으로 코루틴이 취소되어 메모리 누수를 방지할 수 있습니다."

---

## 4. 비동기 동기화

### 왜 동기화가 필요한가?

```kotlin
var counter = 0

// 여러 코루틴이 동시에 접근하면 문제 발생
repeat(1000) {
    launch {
        counter++ // 경쟁 조건(Race Condition) 발생!
    }
}
// counter가 1000이 아닐 수 있음
```

### Mutex (Mutual Exclusion, 상호 배제)

```kotlin
val mutex = Mutex()
var counter = 0

repeat(1000) {
    launch {
        mutex.withLock {
            counter++ // 한 번에 하나의 코루틴만 실행
        }
    }
}
// counter는 반드시 1000
```

- **1개의 코루틴만** 임계 영역(critical section) 진입 가능
- `lock()` / `unlock()` 또는 `withLock { }` 사용
- 코루틴 기반이므로 **스레드를 블로킹하지 않음** (suspend)
- **소유권 개념**: lock을 획득한 코루틴만 unlock 가능

```kotlin
// 실제 사용 예시
class UserRepository {
    private val mutex = Mutex()
    private val cache = mutableMapOf<Int, User>()

    suspend fun getUser(id: Int): User {
        return mutex.withLock {
            cache.getOrPut(id) { fetchUserFromNetwork(id) }
        }
    }
}
```

### Semaphore (세마포어)

```kotlin
// 최대 3개의 코루틴이 동시에 실행 가능
val semaphore = Semaphore(3)

repeat(10) { index ->
    launch {
        semaphore.withPermit {
            println("작업 $index 시작")
            delay(1000) // 동시에 최대 3개만 실행
            println("작업 $index 완료")
        }
    }
}
```

- **N개의 코루틴**이 동시에 임계 영역 진입 가능
- `acquire()` / `release()` 또는 `withPermit { }` 사용
- **동시 API 호출 수 제한**, 커넥션 풀 관리에 적합

```kotlin
// 실제 사용 예시 - 동시 다운로드 수 제한
val downloadSemaphore = Semaphore(3) // 최대 3개 동시 다운로드

suspend fun downloadFiles(urls: List<String>) {
    urls.map { url ->
        launch {
            downloadSemaphore.withPermit {
                downloadFile(url)
            }
        }
    }.joinAll()
}
```

### synchronized (Java/Kotlin 전통 방식)

```kotlin
// 방법 1: synchronized 블록
val lock = Any()
var counter = 0

repeat(1000) {
    Thread {
        synchronized(lock) {
            counter++ // 한 번에 하나의 스레드만 실행
        }
    }.start()
}

// 방법 2: @Synchronized 어노테이션
class Counter {
    private var count = 0

    @Synchronized
    fun increment() {
        count++
    }

    @Synchronized
    fun getCount() = count
}
```

- JVM의 **모니터 락(Monitor Lock)** 기반
- **스레드를 블로킹**함 (코루틴과 함께 쓰면 스레드 낭비 발생)
- lock 객체가 없으면 `this`(인스턴스 자체)를 락으로 사용
- 코루틴 환경에서는 **Mutex 사용을 권장**

```kotlin
// ❌ 코루틴에서 synchronized 사용 - 스레드 블로킹 발생
suspend fun badExample() {
    synchronized(lock) {
        delay(1000) // suspend 함수를 synchronized 블록 안에서 호출 불가!
    }
}

// ✅ 코루틴에서는 Mutex 사용
val mutex = Mutex()
suspend fun goodExample() {
    mutex.withLock {
        delay(1000) // 정상 동작
    }
}
```

### synchronized vs Mutex vs Semaphore 비교

| 구분          | synchronized     | Mutex                  | Semaphore      |
|-------------|------------------|------------------------|----------------|
| **동시 접근**   | 1개               | 1개                     | N개 (설정 가능)     |
| **소유권**     | 있음               | 있음                     | 없음             |
| **스레드 블로킹** | 블로킹              | suspend (비블로킹)         | suspend (비블로킹) |
| **코루틴 호환**  | 제한적              | 완전 호환                  | 완전 호환          |
| **주요 용도**   | 레거시/Java 코드      | 공유 자원 보호               | 동시 접근 수 제한     |

### 면접 대비 모범 답안

> "synchronized는 JVM 모니터 락 기반으로 스레드를 블로킹합니다. 코루틴 환경에서는 synchronized 블록 안에서 suspend 함수를 호출할 수 없고, 스레드를 점유한 채 대기하므로
> 비효율적입니다. 코루틴에서는 스레드를 블로킹하지 않고 코루틴만 중단시키는 Mutex를 사용해야 합니다. Semaphore는 동시에 접근 가능한 코루틴 수를 N개로 제한할 때 사용하며, 예를 들어 API rate
> limit으로 동시 요청을 3개로 제한해야 할 때 Semaphore(3)을 활용합니다."
---

## 5. Threading 기법 비교

### Thread (기본 스레드)

```kotlin
// 직접 Thread 생성
val thread = Thread {
    val result = heavyWork()
    Handler(Looper.getMainLooper()).post {
        updateUI(result)
    }
}
thread.start()
```

- 가장 기본적인 비동기 처리 방법
- 직접 관리해야 하므로 **누수 및 크래시 위험**
- **직접 사용을 권장하지 않음**

---

### Handler / Looper

```kotlin
// Looper: 메시지 큐를 무한 루프로 처리하는 메커니즘
// Handler: Looper에 메시지/Runnable을 전달

// 메인 스레드로 작업 전달
val mainHandler = Handler(Looper.getMainLooper())
mainHandler.post {
    textView.text = "UI 업데이트"
}

// 지연 실행
mainHandler.postDelayed({
    // 2초 후 실행
}, 2000L)

// 백그라운드 스레드에서 Handler 사용
val handlerThread = HandlerThread("BackgroundThread")
handlerThread.start()
val backgroundHandler = Handler(handlerThread.looper)
backgroundHandler.post {
    // 백그라운드 작업
}
```

**Android 메인 스레드 구조**

```
[메인 스레드]
     |
  Looper (무한 루프)
     |
  MessageQueue
     |
  Handler → Message/Runnable 처리
```

- Android UI 스레드의 **근간이 되는 메커니즘**
- `Looper.getMainLooper()`로 메인 스레드에 작업 전달
- View의 내부 동작, `AsyncTask` 등이 이 구조를 기반으로 함
- 현재는 주로 **레거시 코드 유지보수** 또는 **내부 이해** 목적

---

### ThreadPoolExecutor

```kotlin
// 직접 ThreadPool 생성
val executor = ThreadPoolExecutor(
    2,                          // corePoolSize: 기본 유지 스레드 수
    4,                          // maximumPoolSize: 최대 스레드 수
    60L,                        // keepAliveTime
    TimeUnit.SECONDS,
    LinkedBlockingQueue(10)     // 작업 큐
)

executor.execute {
    val result = heavyWork()
    Handler(Looper.getMainLooper()).post { updateUI(result) }
}

// Executors 팩토리 메서드
val fixedPool = Executors.newFixedThreadPool(4)
val singleThread = Executors.newSingleThreadExecutor()
val cachedPool = Executors.newCachedThreadPool()
```

- **스레드 재사용**으로 생성 비용 절약
- 스레드 수를 제어하여 **리소스 관리** 가능
- `OkHttp`, `Retrofit` 내부에서 사용하는 방식
- 직접 사용 시 관리 포인트가 많아 코루틴으로 대체 권장

---

### RxJava

```kotlin
// Observable 생성 및 구독
Observable.fromCallable { fetchDataFromNetwork() }
    .subscribeOn(Schedulers.io())        // 생산: IO 스레드
    .observeOn(AndroidSchedulers.mainThread()) // 소비: 메인 스레드
    .subscribe(
        { data -> updateUI(data) },      // onNext
        { error -> handleError(error) }, // onError
        { /* onComplete */ }
    )

// 여러 스트림 결합
Observable.zip(
    fetchUser(),
    fetchPosts(),
    { user, posts -> Pair(user, posts) }
).subscribe { (user, posts) -> /* ... */ }
```

**특징**

- **함수형 반응형 프로그래밍(FRP)** 패러다임
- `map`, `flatMap`, `zip`, `merge` 등 **강력한 연산자**
- `Disposable`로 구독 해제 관리
- **풍부한 연산자**가 장점이나, 학습 곡선이 가파름
- Kotlin Flow의 등장으로 Android에서는 사용이 줄어드는 추세

---

### Coroutines / Flow

```kotlin
// 코루틴 기본
class MyViewModel : ViewModel() {

    // 단순 비동기 작업
    fun loadData() = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.getData()
        }
        _uiState.value = UiState.Success(data)
    }

    // Flow를 활용한 스트리밍
    val dataStream: Flow<List<Item>> = repository.getItemsFlow()
        .map { items -> items.filter { it.isActive } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

// Compose에서 수집
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val items by viewModel.dataStream.collectAsState()
    // ...
}
```

- **Kotlin 공식 비동기 처리 방식**
- 구조화된 동시성으로 **생명주기 관리 자동화**
- `async/await`으로 **병렬 처리** 가능
- Flow로 **리액티브 스트림** 지원
- 현재 Android 개발의 **표준 비동기 방식**

---

### WorkManager

```kotlin
// Work 정의
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.syncData()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }
}

// Work 요청 및 예약
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval = 1,
    repeatIntervalTimeUnit = TimeUnit.HOURS
)
    .setConstraints(constraints)
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync_work",
    ExistingPeriodicWorkPolicy.KEEP,
    syncRequest
)
```

- **앱 종료, 기기 재시작 후에도 작업 보장**
- 제약 조건(네트워크, 배터리 등) 설정 가능
- **백그라운드 작업 제한 정책**에 자동 대응
- 주기적 작업, 체인 작업 지원
- **코루틴 기반**: `CoroutineWorker` 사용 권장

---

### Threading 총정리

| 방법                     | 출시 시기  | 상태     | 주요 용도        |
|------------------------|--------|--------|--------------|
| **Thread**             | 초기     | 비권장    | 기본 이해용       |
| **Handler/Looper**     | 초기     | 유지     | 스레드 간 메시지 전달 |
| **ThreadPoolExecutor** | API 초기 | 유지     | 스레드 풀 직접 제어  |
| **RxJava**             | 2013년~ | 레거시    | 복잡한 스트림 처리   |
| **Coroutines/Flow**    | 2018년~ | **표준** | 모든 비동기 처리    |
| **WorkManager**        | 2018년~ | **표준** | 보장된 백그라운드 작업 |

---

## 6. Dispatchers

### 개요

Dispatcher는 **코루틴이 어떤 스레드(풀)에서 실행될지** 결정한다.

```kotlin
// Dispatcher 지정 방법
viewModelScope.launch(Dispatchers.IO) {
    // IO 스레드에서 실행
}

withContext(Dispatchers.Default) {
    // Default 스레드에서 실행
}
```

### Dispatchers.Main

```kotlin
viewModelScope.launch(Dispatchers.Main) {
    textView.text = "UI 업데이트" // 메인 스레드에서 실행
}

// .immediate: 이미 메인 스레드라면 즉시 실행 (스케줄링 생략)
launch(Dispatchers.Main.immediate) {
    // 불필요한 스케줄링 없이 즉시 실행
}
```

- **메인(UI) 스레드**에서 실행
- UI 업데이트, LiveData/StateFlow 값 변경에 사용
- **CPU 집약적 작업 금지** (ANR 발생 위험)

### Dispatchers.IO

```kotlin
viewModelScope.launch {
    val data = withContext(Dispatchers.IO) {
        // 네트워크 요청
        api.getData()
        // 파일 읽기/쓰기
        File("data.txt").readText()
        // DB 쿼리
        dao.getAll()
    }
}
```

- **I/O 작업** 전용 (네트워크, 파일, DB)
- 스레드 풀 크기: `max(64, CPU 코어 수)`
- 스레드가 대기(blocking)하는 동안 다른 스레드 활용
- `Default`와 **스레드 풀 공유** (독립적으로 동작하도록 최적화됨)

### Dispatchers.Default

```kotlin
viewModelScope.launch {
    val result = withContext(Dispatchers.Default) {
        // CPU 집약적 연산
        largeList.sortedBy { it.score }
        // 이미지 처리, JSON 파싱 등
        parseHeavyJson(jsonString)
    }
}
```

- **CPU 집약적 작업** 전용 (정렬, 파싱, 연산)
- 스레드 풀 크기: **CPU 코어 수** (기본값)
- 스레드가 항상 CPU를 사용하는 작업에 최적화

### Dispatchers.Unconfined

```kotlin
launch(Dispatchers.Unconfined) {
    println("스레드: ${Thread.currentThread().name}") // 호출자 스레드
    delay(100)
    println("스레드: ${Thread.currentThread().name}") // delay 후 다른 스레드
}
```

- **특정 스레드에 국한되지 않음**
- suspend 지점 이전에는 호출자 스레드, 이후에는 재개한 스레드에서 실행
- **일반 개발에서는 사용 비권장** (예측하기 어려운 동작)
- 주로 테스트 코드에서 활용

### withContext vs launch 선택 기준

```kotlin
// withContext: 결과값이 필요한 경우
val data = withContext(Dispatchers.IO) {
    repository.getData() // 반환값 있음
}

// launch: 결과값이 필요 없는 경우
launch(Dispatchers.IO) {
    repository.syncData() // 반환값 불필요
}

// async/await: 병렬 처리 후 결과값이 필요한 경우
val (user, posts) = coroutineScope {
    val userDeferred = async(Dispatchers.IO) { repository.getUser() }
    val postsDeferred = async(Dispatchers.IO) { repository.getPosts() }
    Pair(userDeferred.await(), postsDeferred.await())
}
```

### Dispatcher 선택 가이드

```
작업 종류에 따른 Dispatcher 선택:

네트워크 요청     → Dispatchers.IO
파일 읽기/쓰기    → Dispatchers.IO
DB 쿼리          → Dispatchers.IO
이미지 처리       → Dispatchers.Default
JSON 파싱         → Dispatchers.Default
리스트 정렬       → Dispatchers.Default
UI 업데이트       → Dispatchers.Main
```

### 면접 대비 모범 답안

> "Dispatchers.IO와 Default의 차이는 스레드 풀 크기와 목적에 있습니다. IO는 스레드가 대부분 대기 상태인 I/O 작업을 위해 스레드를 많이 유지하고, Default는 CPU를 계속 사용하는
> 연산을 위해 코어 수만큼만 스레드를 유지합니다. 잘못된 Dispatcher 사용은 성능 저하나 ANR을 유발할 수 있습니다."

---

## 전체 요약

| 개념                      | 한 줄 정리                                         |
|-------------------------|------------------------------------------------|
| **Cold Stream**         | 구독 시마다 새로 시작하는 1:1 스트림 (Flow)                  |
| **Hot Stream**          | 구독 여부와 무관하게 흐르는 공유 스트림 (SharedFlow, StateFlow) |
| **Flow**                | Cold 스트림, 1회성 비동기 작업, suspend 조합               |
| **SharedFlow**          | Hot 스트림, 이벤트 전달, replay 설정 가능                  |
| **StateFlow**           | Hot 스트림, UI 상태 관리, 항상 최신값 보유                   |
| **코루틴**                 | 경량 비동기 실행 단위, 스레드 블로킹 없이 중단/재개                 |
| **Mutex**               | 1개의 코루틴만 임계 영역 접근 허용                           |
| **Semaphore**           | N개의 코루틴까지 동시 접근 허용                             |
| **WorkManager**         | 앱 종료/재시작 후에도 보장되는 백그라운드 작업                     |
| **Dispatchers.IO**      | I/O 작업용, 대형 스레드 풀                              |
| **Dispatchers.Default** | CPU 연산용, 코어 수만큼 스레드                            |
| **Dispatchers.Main**    | UI 업데이트용, 메인 스레드                               |
