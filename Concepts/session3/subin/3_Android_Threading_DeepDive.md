# Threading 심화

## Thread / Handler·Looper / ThreadPoolExecutor / RxJava / Coroutines·Flow / WorkManager / Dispatchers

## 전체 핵심 요약

- Android에서 “스레딩”은 결국 **Main Thread를 안 막고**, **필요한 곳에서 적절히 전환**하는 기술
- 과거: `Thread + Handler` / `Executor` / `RxJava`
- 현재 표준: **Coroutines + Dispatchers + Flow**
- “앱이 꺼져도 보장 실행”은 **WorkManager**가 담당(스레딩 도구가 아니라 작업 보장 도구)

---

## 1) Main Thread / MessageQueue / Looper / Handler

### Main Thread가 하는 일

- UI 그리기, 입력 처리, 생명주기 콜백 처리
- **여기서 오래 걸리면 ANR/프리징**

### Looper / MessageQueue

- Main Thread에는 Looper가 있고, 작업(Runnable/Message)을 Queue에 쌓아 순서대로 처리

### Handler

- MessageQueue에 작업을 넣는 인터페이스
- `post`, `postDelayed`로 예약 실행 가능

**요점**

- “UI 스레드 전환”의 원조 메커니즘
- 코루틴 `Dispatchers.Main`도 결국 Main 큐에서 실행되는 형태

---

## 2) Thread / Executor / ThreadPoolExecutor (풀 & 정책)

### Thread를 직접 쓰면 생기는 문제

- 생성/관리 비용
- 취소/에러 전파/수명 관리가 어려움
- 스레드를 많이 만들면 문맥 전환/메모리 부담 증가

### Executor / ThreadPoolExecutor의 역할

- 스레드를 “풀로 재사용”
- 동시 실행 개수/큐/거절 정책 등 운영 정책을 제어

**ThreadPoolExecutor에서 중요한 감각**

- `corePoolSize / maxPoolSize / workQueue / RejectedExecutionHandler`
- “동시성”은 무조건 높일수록 좋은 게 아니라 디바이스 자원에 맞춰 조절해야 함

---

## 3) RxJava의 Threading 모델 (subscribeOn / observeOn)

Rx는 “스트림”이지만, 스레딩 전략이 명확해서 면접/레거시에서 자주 나옴

- `subscribeOn()` : upstream(생산/작업)을 어느 스레드에서 할지
- `observeOn()` : downstream(소비/UI)을 어느 스레드에서 할지

**요점**

- Rx는 스레딩 전환을 연산자로 선언적으로 표현함
- Flow에서는 `flowOn`(업스트림) + collect 컨텍스트(다운스트림)로 비슷한 구조가 나옴

---

## 4) Coroutines / Dispatchers / withContext (현재 표준 스레딩)

### Dispatchers 감각

- Main: UI
- IO: 네트워크/DB/파일 I/O
- Default: CPU 작업(파싱/정렬/연산)

### withContext

- “이 블록은 IO에서 돌려” 같은 명시적 전환

**요점**

- 코루틴은 “작업 단위”, Dispatcher는 “실행 위치”
- 구조적 동시성(scope 기반)으로 수명 관리가 쉬움

---

## 5) WorkManager는 ‘스레딩’이 아니라 ‘작업 보장’

- WorkManager는 “스레드에서 어떻게 돌릴지”가 아니라
    - 조건(네트워크/충전)
    - 재시도/백오프
    - 앱 종료 후에도 실행 보장을 제공하는 **Job Orchestration** 도구에 가까움

---

## 6) 선택 기준(스레딩 관점)

- UI 전환/예약 실행: Handler(기반) / Dispatchers.Main(현대)
- CPU/IO 병렬 처리: ThreadPoolExecutor(직접) / Dispatchers.Default·IO(현대)
- 스트림 기반 비동기 파이프라인: Rx / Flow
- 앱 꺼져도 보장 실행: WorkManager
