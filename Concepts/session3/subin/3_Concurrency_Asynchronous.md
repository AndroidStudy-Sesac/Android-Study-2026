# Android 동시성/비동기

## 전체 핵심 요약

- **실행 단위**: Thread vs Coroutine(코루틴은 작업 단위, 스레드 위에서 실행)
- **스케줄링/전환**: Handler/Looper(메시지 큐), ThreadPoolExecutor(풀), Dispatchers(코루틴의 실행 컨텍스트)
- **스트림/반응형**: Flow(Cold 기본), StateFlow/SharedFlow(Hot), RxJava(반응형 라이브러리)
- **동기화**: Mutex/Semaphore로 공유 자원 경쟁을 제어
- **작업 보장**: WorkManager는 앱이 꺼져도 “조건/재시도/보장 실행”을 담당

---

# 1) 실행 단위 & 동시성 모델 (Thread vs Coroutine)

## 1-1. Thread (OS 스레드)

- OS가 스케줄링하는 실행 단위
- 블로킹 호출(예: I/O, sleep)이 발생하면 해당 스레드는 그동안 다른 일을 못 함
- 스레드를 많이 만들면 메모리/문맥 전환 비용이 커짐 → 풀(Executor)로 관리하는 이유

## 1-2. Coroutine (코루틴)

- OS 스레드가 아니라 **중단/재개 가능한 작업 단위**
- 실제 실행은 Dispatcher가 제공하는 스레드(풀) 위에서 일어남
- `suspend`는 “스레드를 막지 않고” 중단/재개되는 함수라는 의미

### 감각적으로

- Thread: “좌석(실행 자리)” 자체
- Coroutine: “업무 티켓(작업 흐름)” → 좌석을 번갈아 사용

---

# 2) 스케줄링 & 스레드 전환

이 파트는 “스레딩 도구”들의 역할을 정확히 구분하는 게 핵심.

---

## 2-1. Thread (직접 생성/실행)

- 가장 원초적인 방법
- 짧은 실험/학습에는 쉽지만, 실무에서는 관리(취소/예외/풀/수명)가 어려워 잘 안 씀

---

## 2-2. Handler / Looper (Main Thread와 MessageQueue)

### 핵심 개념

- Android의 Main Thread에는 **Looper + MessageQueue**가 있고,
- Handler는 그 큐에 **메시지/작업(Runnable)** 을 넣어 “언제 실행할지” 예약한다.

### 언제 쓰나

- UI 스레드로 안전하게 돌아와서 UI 갱신해야 할 때(전통적 방식)
- 특정 시간 뒤 실행(postDelayed)
- (요즘은 코루틴 `Dispatchers.Main`으로 대체되는 경우가 많지만, 기반 개념은 중요)

---

## 2-3. ThreadPoolExecutor (스레드 풀)

- 스레드를 매번 만들지 않고, **미리 만든 스레드 풀에 작업을 던지는 방식**
- “동시에 처리할 작업이 많을 때” 관리가 쉬워짐
- 풀 크기/큐 정책/거절 정책 등 제어 가능(성능 튜닝 포인트)

### 실무 감각

- CPU 작업/병렬 처리, 백그라운드 작업을 풀로 관리할 때 사용
- 코루틴의 Dispatcher도 내부적으로는 “풀”을 사용한다는 점에서 연결됨

---

## 2-4. Dispatchers (코루틴의 스케줄러/실행 컨텍스트)

### 핵심

- Dispatcher는 코루틴이 **어떤 스레드(또는 풀)에서 실행될지** 결정

대표:

- `Dispatchers.Main`: UI 스레드
- `Dispatchers.IO`: I/O 최적화 풀(네트워크/DB/파일)
- `Dispatchers.Default`: CPU 작업 풀(정렬/파싱/계산)

### 중요한 포인트

- Main에서 I/O 돌리면 UI 프리징
- IO에서 UI 업데이트하면 예외/오작동 → 보통 `withContext(Main)`로 복귀

---

## 2-5. RxJava (Threading 관점에서의 위치)

RxJava 자체는 “반응형 스트림”이지만, 스레딩 도구로도 자주 등장함.

- `subscribeOn(...)`: upstream(생산/작업)을 어느 스레드에서 할지
- `observeOn(...)`: downstream(관찰/소비)을 어느 스레드에서 할지

→ 이 개념은 Flow의 `flowOn`(업스트림 전환)과 유사한 비교 포인트가 됨

---

# 3) 스트림/반응형: Flow / StateFlow / SharedFlow + Hot/Cold

## 3-1. Flow (Cold Stream 기본)

- `Flow<T>`는 “시간에 따라 여러 값을 방출하는 비동기 스트림”
- 기본적으로 **cold**: collect(수집)하기 전에는 실행되지 않음
- collect를 여러 번 하면 작업이 여러 번 수행될 수 있음(중복 실행 주의)

### Flow에서 자주 쓰는 연산자

- `map`, `filter`, `onEach`, `catch`, `flowOn`
- 컬렉션 고차함수(map/filter)와 동일한 감각으로 스트림을 변환

---

## 3-2. Hot Stream vs Cold Stream

### Cold

- 구독(collect)할 때마다 새로 시작
- 예: 기본 Flow, suspend 함수(값 1번), 대부분의 시퀀스형 흐름

### Hot

- 생산이 독립적으로 계속 돌고, 구독자는 “현재 흐름에 합류”
- 예: StateFlow, SharedFlow(설정에 따라), Rx의 Subject 계열

> 한 줄 감각
> 
> 
> Cold = “요청하면 시작”
> 
> Hot = “항상 흐르고 있고 참여”
> 

---

## 3-3. StateFlow (상태)

- **항상 현재 값**을 가진 Hot stream
- 새로 수집해도 최신 값부터 즉시 받음
- UI 상태(로딩/데이터/에러)를 표현하기 최적

### 실무 감각

- 화면 상태는 StateFlow로 두는 게 자연스럽고 안정적
- `data class UiState + copy()`와 궁합이 매우 좋음

---

## 3-4. SharedFlow (이벤트/브로드캐스트)

- 여러 구독자에게 이벤트를 “공유”하는 Hot stream
- 토스트/네비게이션 같은 **일회성 이벤트** 처리에 잘 맞음
- replay/buffer 설정으로 이벤트 재전달 여부를 조절 가능

### 실무 감각

- “이벤트를 StateFlow로 처리하면 재구독 시 중복 발생” 문제가 생길 수 있음
- 그래서 상태(StateFlow)와 이벤트(SharedFlow)를 분리하는 패턴이 흔함

---

## 3-5. RxJava는 여기서 어떤 위치?

- RxJava는 Flow보다 오래된(혹은 더 넓은) 반응형 스트림 라이브러리
- Observable/Flowable/Single/Maybe/Completable 같은 타입 체계로 스트림을 모델링
- Android에선 과거에 표준급이었고, 현재는 Coroutines/Flow로 이동한 프로젝트가 많음
- 다만 “기존 코드/레거시/특정 연산(backpressure 등)”에서 여전히 만날 수 있음

---

# 4) 동기화/공유자원 제어: Mutex / Semaphore (비동기에서 꼭 터짐)

여기부터는 “비동기 자체”가 아니라 **동시성으로 인한 문제(경쟁/충돌)**를 해결하는 도구

## 4-1. 왜 동기화가 필요하나?

- 여러 작업이 동시에 같은 데이터를 건드리면
    - 레이스 컨디션(race condition)
    - 데이터 손상/중복 실행
    - 순서 꼬임이 발생할 수 있음

---

## 4-2. Mutex (상호 배제)

- 한 번에 한 작업만 임계 구역에 들어갈 수 있게 하는 락
- 코루틴 환경에서는 `Mutex`를 사용해 suspend-friendly하게 잠금 가능

### 실무 감각

- 동시에 들어오면 안 되는 저장/갱신 작업에 적용
- 예: 토큰 갱신(refresh token)을 동시에 여러 번 호출하지 않게 막기

---

## 4-3. Semaphore (동시 실행 개수 제한)

- 동시에 N개까지만 허용
- Mutex가 1개라면, Semaphore는 N개로 확장된 버전

### 실무 감각

- 네트워크 요청을 동시에 너무 많이 날리지 않도록 제한
- 이미지/파일 처리 작업의 동시 실행 수 제한

---

# 5) 작업 보장/백그라운드 실행: WorkManager

## 5-1. WorkManager의 역할

- 앱이 종료/재부팅/백그라운드 제한이 있어도
    
    “언젠가 반드시 실행”되어야 하는 작업을 시스템이 관리
    

## 5-2. 핵심 기능

- 제약조건(네트워크, 충전 중 등)
- 재시도/백오프
- 작업 체이닝
- OS 버전별 백그라운드 정책을 내부적으로 흡수

## 5-3. 실무 감각

- 업로드 재시도, 동기화, 로그 전송, 예약 실행 등 “보장 실행” 영역의 표준 도구
- “지속 실행”이 목적이면 Service(특히 Foreground)가 더 적합

---

# 최종 정리

- **Thread/Handler/Executor/Dispatcher**: 어디서 실행할지(스케줄링/스레딩)
- **RxJava/Flow(StateFlow/SharedFlow)**: 값이 흐르는 방식(스트림)
- **Mutex/Semaphore**: 동시에 접근하면 터지는 문제 해결(동기화)
- **WorkManager**: 앱이 꺼져도 실행되어야 하는 작업 보장
