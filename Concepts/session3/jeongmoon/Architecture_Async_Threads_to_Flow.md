> ### 핵심 요약
> - 비동기 처리의 핵심: Coroutine을 사용해 메인 스레드를 막지 않고 작업을 수행합니다.
> - 데이터 흐름 관리: Flow를 통해 비동기 데이터를 일관된 스트림 형태로 처리합니다.
> - 상태 vs 이벤트 분리: StateFlow는 UI 상태를, SharedFlow는 일회성 이벤트를 담당합니다.
> - 안정성 확보: Mutex로 동시성 문제를 제어하고, WorkManager로 작업 실행을 보장합니다.

# 안드로이드 비동기 작업 총정리 (Android Asynchrony Guide)
## 목차
- 전체 구조 한눈에 보기
- 비동기(Asynchronous)의 이해
- Thread vs Coroutine
- 안드로이드 비동기 진화 과정
- Stream: Cold vs Hot
- Flow / StateFlow / SharedFlow 비교
- 동시성 제어 (Mutex & Semaphore)
- Dispatchers (스레드 분배기)
- WorkManager (백그라운드 보장)
- 💡 실무 선택 가이드
- 실무 아키텍처 데이터 흐름

## 1. 전체 구조 한눈에 보기
안드로이드의 비동기 작업은 아래와 같은 계층 구조를 가집니다. 밑으로 갈수록 더 추상화되고 다루기 쉬운 형태입니다.

```text
Thread (OS가 관리하는 실제 실행 단위)
 └─ Coroutine (스레드 위에서 도는 가벼운 작업 단위)
      └─ Flow (코루틴 위에서 흐르는 데이터 스트림)
           ├─ StateFlow (상태를 저장하는 Flow)
           └─ SharedFlow (이벤트를 전달하는 Flow)
```

---

## 2. 비동기(Asynchronous)의 이해
> "요청을 보내놓고 결과가 올 때까지 멈춰있지 않고, 다른 작업을 계속 수행하는 방식 (Non-blocking)"

💡 왜 필요할까?
- UI 멈춤 방지 (ANR 예방): 메인 스레드가 네트워크 통신 등으로 멈춰있으면 앱이 굳어버립니다.
- 사용자 경험(UX) 개선: 부드러운 화면 전환과 빠른 반응성을 제공하기 위함입니다.

---

## 3. Thread vs Coroutine
현재 안드로이드 개발의 표준은 Coroutine입니다. Thread를 직접 다루는 일은 이제 거의 없습니다.

| **구분** | **Thread (스레드)** | **Coroutine (코루틴)** |
| --- | --- | --- |
| **관리 주체** | OS (운영체제) | 사용자 (Kotlin 런타임) |
| **무게** | 무거움 (생성 및 전환 비용 큼) | **매우 가벼움** (하나의 스레드에 수천 개 생성 가능) |
| **특징** | Context Switching 비용이 큼 | '구조화된 동시성' 지원으로 메모리 누수 방지 |
| **코드 예시** | `Thread { ... }.start()` | `viewModelScope.launch { ... }` |

> Thread 직접 사용 ❌ / Coroutine 사용 ✅

---

## 4. 안드로이드 비동기 진화 과정
- Handler / Looper: 백그라운드 스레드에서 메인 스레드(UI)로 메시지를 던지는 방식. 코드가 복잡해지는 단점(콜백 지옥).
- ThreadPoolExecutor: 스레드를 매번 만들지 않고 '풀(Pool)'에 모아 재사용. 성능은 개선되었으나 여전히 무거움.
- RxJava: Reactive Stream 기반의 끝판왕. 매우 강력하지만 학습 곡선이 수직벽에 가까움.
- Coroutines / Flow (현재 표준): Kotlin 언어 내장 기능으로 가볍고 직관적이며, 비동기 코드를 동기 코드처럼 읽기 쉽게 작성 가능.

---

## 5. Stream 핵심 개념
데이터가 물 흐르듯 들어오는 것을 'Stream'이라고 하며, Flow를 이해하기 위해선 두 가지 온도를 알아야 합니다.
- ❄️ Cold Stream (일반 Flow)
  - 특징: 누군가 요청(collect)해야만 실행됨. 수집할 때마다 처음부터 새로 실행됨. (마치 넷플릭스 VOD)
  - 용도: API 호출, DB 읽기 등 1회성 데이터 요청.
- 🔥 Hot Stream (StateFlow, SharedFlow)
  - 특징: 구독자가 있든 없든 일단 실행되어 흐름. 구독 시점부터 발생하는 데이터를 받음. (마치 라디오 생방송)
 
---

## 6. Flow / StateFlow / SharedFlow 비교
가장 많이 헷갈리지만, 아키텍처 설계 시 가장 중요한 3인방입니다.

| **타입** | **특징** | **주 사용처** |
| --- | --- | --- |
| **Flow** | Cold Stream / 상태 저장 안 함 | 1회성 데이터 요청 (API, Room DB) |
| **StateFlow** | Hot Stream / **항상 최신 값 1개 유지** / 초기값 필요 | **UI 상태 관리** (LiveData 완벽 대체) |
| **SharedFlow** | Hot Stream / 이벤트 브로드캐스트 / 초기값 없음 | **일회성 이벤트** (Toast, SnackBar, 화면 이동) |

```kotlin
// 1. Flow (데이터)
fun getUsers(): Flow<List<User>> = repository.fetchUsers()

// 2. StateFlow (UI 상태)
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// 3. SharedFlow (UI 이벤트)
private val _uiEvent = MutableSharedFlow<Event>()
val uiEvent: SharedFlow<Event> = _uiEvent.asSharedFlow()
```

---

## 7. 동시성 제어
여러 코루틴이 동시에 하나의 자원에 접근할 때(예: 좋아요 버튼 연타) 발생할 수 있는 문제를 막는 문지기입니다.

- 🔐 Mutex (Mutual Exclusion)
  - 기능: 한 번에 1개의 코루틴만 접근 허용 (화장실 열쇠 1개)
  - `mutex.withLock { /* 안전한 작업 */ }`
- 🚦 Semaphore
  - 기능: N개까지 동시 접근 허용 (식당 빈자리 수) 
  - `val semaphore = Semaphore(3)`

 > 사용 기준: 데이터 무결성 보호 → Mutex / 동시 API 요청 수 제한 → Semaphore

---

## 8. Dispatchers
코루틴을 어떤 종류의 스레드(택시)에 태워 보낼지 결정하는 배차원입니다.

| **Dispatcher** | **용도** | **설명** |
| --- | --- | --- |
| **Main** | UI 작업 | 화면 그리기, LiveData/StateFlow 업데이트 |
| **IO** | 네트워크 / DB | API 통신, 파일 읽고 쓰기 (가장 많이 사용됨) |
| **Default** | CPU 연산 | 무거운 리스트 정렬, JSON 파싱 등 |

---

## 9. WorkManager
앱이 꺼지거나 스마트폰이 재부팅 되어도 **"반드시 끝마쳐야 하는 작업"**을 보장합니다.
- 특징: 백그라운드 실행 보장, 작업 실패 시 자동 재시도, 기기 상태(배터리, 와이파이)에 따른 실행 조건 설정 가능.
- 사용 예: 대용량 파일 업로드/다운로드, 로컬 로그 서버 전송, 주기적인 데이터 동기화.

 > 사용 기준: 사용자가 앱을 보고 있을 때 → Coroutine / 앱 종료 이후에도 보장되어야 할 때 → WorkManager

---

## 10. 💡 실무 선택 가이드
실무에서 "이 기능은 뭘로 구현하지?" 고민될 때 보는 치트시트입니다.

| **구현 목표** | **추천 기술 스택** |
| --- | --- |
| **화면의 UI 상태 (로딩, 데이터, 에러)** | `StateFlow` |
| **일회성 알림 (Toast, Navigation)** | `SharedFlow` |
| **서버 API 호출 / 로컬 DB 쿼리** | `Flow` + `Coroutine` + `Dispatchers.IO` |
| **앱을 꺼도 진행되어야 하는 파일 업로드** | `WorkManager` |
| **공유 데이터 동시 수정 방지** | `Mutex` |

---

## 11. 실무 아키텍처 데이터 흐름
UI 레이어에서 데이터 레이어까지 비동기 처리가 어떻게 흘러가는지 보여주는 일반적인 구조입니다.

```text
[UI Layer (Activity / Compose)]  <-- 관찰 (collect)
    ↓ (Action)
[ViewModel]                      <-- StateFlow / SharedFlow 로 가공 및 노출
    ↓ (Request)
[Repository]                     <-- Flow 전달
    ↓ (Fetch)
[Data Source (API / Room)]       <-- Coroutine + Dispatchers.IO 활용
```

## 최종 한 줄 정리

> Coroutine으로 비동기를 실행하고, Flow로 데이터를 흐르게 하며,
> StateFlow와 SharedFlow로 상태와 이벤트를 분리하고,
> Mutex와 WorkManager로 안정성을 보장한다.
