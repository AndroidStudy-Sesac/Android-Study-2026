# Android 4대 컴포넌트 Part 2 — Service

## 전체 핵심 요약

- Service는 **UI 없이 백그라운드에서 작업**을 수행하는 컴포넌트다.
- 크게 **Started Service**(작업 실행)와 **Bound Service**(클라이언트와 연결)로 나뉜다.
- **장시간/사용자 인지 작업은 Foreground Service(알림 필수)** 를 사용한다.
- 반드시 실행되어야 하는 지연/예약 작업은 보통 **WorkManager**가 더 적합하다

---

# 1) Service가 하는 일 (정의/역할)

## 핵심 역할

- UI 없이 작업 수행: 재생, 위치 추적, 통신, 동기화 등
- 앱 컴포넌트(Activity/Fragment 등)와 별개로 동작 가능
- 단, 프로세스가 죽으면 같이 죽을 수 있으므로 “보장 실행” 목적이면 WorkManager 고려

---

# 2) Service 종류: Started vs Bound

## 2-1) Started Service

### 개념

- `startService()` / `startForegroundService()`로 시작
- Service가 **독립적으로 작업을 수행**
- 보통 작업 종료 시 `stopSelf()` 또는 `stopService()`로 종료

### 생명주기 핵심 콜백

- `onCreate()` → `onStartCommand()` → (작업 수행) → `onDestroy()`

### 실무 감각

- “음악 재생”, “지속 동기화”, “장시간 작업” 같은 케이스에서 등장
    
    (하지만 장시간이면 Foreground로 가야 함)
    

---

## 2-2) Bound Service

### 개념

- `bindService()`로 연결
- 클라이언트(Activity 등)가 Service에 바인딩하여 메서드 호출/데이터 교환 가능
- 모든 클라이언트가 unbind하면 종료될 수 있음(Started가 아니라면)

### 생명주기 핵심 콜백

- `onCreate()` → `onBind()` → (연결 유지) → `onUnbind()` → `onDestroy()`

### 실무 감각

- 앱 내부에서만 쓰는 장기 연결(예: 음악 플레이어 상태, BLE, 소켓 연결)에서 사용
- UI와 Service가 긴밀히 통신해야 할 때

---

# 3) Foreground Service (가장 중요)

## 3-1) 왜 필요한가?

Android는 백그라운드에서 오래 실행되는 작업을 제한합니다.

사용자가 인지해야 하는 지속 작업은 **Foreground Service**로 올려서 실행해야 안정적입니다.

## 핵심 특징

- **지속 알림(Notification) 필수**
- 시스템이 상대적으로 죽이기 어렵게 유지
- 단, 남용하면 UX/정책 이슈(알림 상시 노출)

---

## 3-2) 언제 Foreground Service를 쓰나?

- 음악 재생(백그라운드에서도 계속)
- 위치 추적(내비게이션, 운동 기록)
- 통화/녹음/미러링 같이 사용자가 즉시 인지해야 하는 지속 작업

---

## 3-3) 예제: Foreground Service 시작 흐름(최소)

### (1) Service 시작: `startForegroundService`

```kotlin
val intent = Intent(this, MyForegroundService::class.java)
ContextCompat.startForegroundService(this, intent)
```

### (2) Service 내부에서 빠르게 `startForeground()`

```kotlin
class MyForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        val notification = buildNotification()
        startForeground(1, notification) // 핵심
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 실제 작업 시작
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
```

> ✅ 포인트
> 
> 
> Foreground Service는 시작만이 아니라 **Service 내부에서 startForeground를 빨리 호출**해야 안전합니다.
> 

---

# 4) Android 백그라운드 제한(왜 “그냥 Service”가 안 되는가)

## 핵심

- 최근 Android는 백그라운드에서 Service 실행/유지를 제한함
- 앱이 백그라운드에 있을 때 무제한으로 작업을 돌리면 배터리/자원 낭비가 커서 정책적으로 막음

### 실무 결론

- 지속 실행 + 사용자 인지 필요 → Foreground Service
- 나중에 반드시 실행되어야 하는 작업(예약/재시도) → WorkManager
- 즉시 짧게 끝나는 작업 → 코루틴/스레드(앱이 살아있는 동안)

---

# 5) onStartCommand 리턴 값 감각

- `START_NOT_STICKY`: 죽으면 자동 재시작 X (작업 재의미 없음)
- `START_STICKY`: 죽으면 재시작 시도(인텐트 null일 수 있음)
- `START_REDELIVER_INTENT`: 죽으면 intent 다시 전달하며 재시작

✅ 실무 감각: 서비스가 죽었을 때 어떻게 복구할지 정책을 정하는 값

---

# 6) 정리: 어떤 걸 선택해야 하나?

- **앱이 켜져 있는 동안만 짧게**: 코루틴/스레드
- **반드시 실행 + 조건/재시도 필요**: WorkManager
- **사용자가 인지해야 하는 장시간 작업**: Foreground Service
- **UI와 연결해서 상태/제어 필요**: Bound Service(또는 Started+Bound 혼합)

---

# Service vs WorkManager vs Coroutine

## 전체 핵심 요약

- **Coroutine(코루틴)**: 앱 프로세스가 살아있는 동안 빠르고 간단한 비동기 처리(즉시 작업)
- **Service**: UI 없이 백그라운드 작업 수행. 장시간/지속 작업이면 **Foreground Service**가 사실상 필수
- **WorkManager**: 앱이 종료/재부팅/백그라운드에서도 “언젠가 반드시 실행”되어야 하는 작업을 **조건/재시도**와 함께 보장

> ✅ 한 줄 결론
> 
> 
> **즉시/짧게(프로세스 내)** = Coroutine
> 
> **지속 실행 + 사용자 인지** = Foreground Service
> 
> **예약/보장 실행 + 조건/재시도** = WorkManager
> 

---

# 1) 선택 기준 표

| 상황/요구사항 | Coroutine | Service | WorkManager |
| --- | --- | --- | --- |
| 앱이 켜져 있는 동안만 실행 | ✅ 최적 | 가능하지만 과함 | 가능하지만 과함 |
| 앱이 백그라운드로 가도 계속 실행 | ⚠️ 프로세스 죽으면 끝 | ✅ 가능(단 제한 큼) | ✅ 가능 |
| 장시간 지속(음악/위치/통화 등) | ❌ 부적합 | ✅ **Foreground Service** | ❌ 부적합(즉시/지속 실행 용도 아님) |
| 반드시 실행되어야 함(언젠가) | ❌ 보장 X | ⚠️ 제한/중단 가능 | ✅ **보장 강함** |
| 네트워크 연결 시에만 실행 | 직접 구현 필요 | 직접 구현 필요 | ✅ Constraints로 자연 |
| 실패 시 자동 재시도 | 직접 구현 | 직접 구현 | ✅ 기본 지원(재시도 설계 용이) |
| 정확한 시간에 실행(알람처럼) | ❌ | ❌ | ⚠️ “정확한 시간”은 약함(키워드) |
| UI와 상태 공유/제어 필요 | ✅ ViewModel/StateFlow | ✅ Bound Service 가능 | ❌ 주로 일회성 작업 |
