# Android 4대 컴포넌트 Part 3 — BroadcastReceiver

## 전체 핵심 요약

- BroadcastReceiver는 시스템/앱이 뿌리는 **브로드캐스트 메시지(이벤트)** 를 받아서 처리하는 컴포넌트다.
- 수신 방식은 크게 2가지:
    - **Manifest 등록**: 앱이 실행 중이 아니어도(일부는) 받을 수 있음
    - **Dynamic 등록**: 앱 실행 중에만 받음(수명 관리 필요)
- Receiver의 `onReceive()`는 **짧게 처리**해야 하며 오래 걸리는 작업은 **WorkManager/Service로 위임**하는 게 기본.
- Android 버전에 따라 암시적 브로드캐스트 제한 등 제약이 커서 실무에서는 **필요한 것만** 정확히 수신하는 설계가 중요.

---

# 1) BroadcastReceiver 개념

## 1-1) Broadcast란?

- 시스템/앱이 특정 이벤트를 “전체/특정 대상”에게 알리는 방식
    
    예: 부팅 완료, 네트워크 변화, 배터리 상태, 알림 액션 클릭 등
    

## 1-2) Receiver 역할

- 브로드캐스트를 수신하고 `onReceive()`에서 반응
- 단, **Receiver는 UI 컴포넌트가 아니고**, 실행 시간이 짧아야 함

---

# 2) 등록 방식: Manifest vs Dynamic

## 2-1) Manifest 등록 (정적 등록)

### 특징

- 앱 프로세스가 실행 중이 아니어도 특정 브로드캐스트를 받을 수 있는 경우가 있음
- 하지만 최신 Android에서는 제한이 많음(특히 암시적 브로드캐스트)

### 예시(개념)

```xml
<receiver
    android:name=".BootReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

---

## 2-2) Dynamic 등록 (동적 등록)

### 특징

- 코드에서 `registerReceiver()`로 등록
- 등록한 컴포넌트(Activity/Service 등)가 살아있는 동안만 수신
- **반드시 해제(unregisterReceiver)** 해야 누수/크래시 방지

### 예시(간단)

```kotlin
private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 이벤트 처리
    }
}

override fun onStart() {
    super.onStart()
    registerReceiver(receiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
}

override fun onStop() {
    super.onStop()
    unregisterReceiver(receiver)
}
```

---

# 4) 제약/주의점

## 4-1) onReceive는 짧게

- onReceive는 메인 스레드에서 호출될 수 있고, 오래 걸리면 ANR 위험
- 긴 작업은 **WorkManager / Foreground Service**로 위임

## 4-2) 암시적 브로드캐스트 제한

- 최신 Android는 앱이 원하는 모든 시스템 브로드캐스트를 마음대로 받지 못하게 제한
- 따라서 정말 필요한 브로드캐스트만 설계하고 대안(WorkManager) 고려

## 4-3) exported 이슈(보안)

- 외부 앱이 브로드캐스트로 내 Receiver를 호출할 수 있는지(`exported`)는 보안과 직결
- 내부용이면 `exported=false` 기본 마인드

---

# 5) Android 예제 (필수)

## 예제 1) BOOT_COMPLETED → WorkManager로 동기화 예약(구조 예시)

```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // onReceive에서는 짧게: 작업 예약만!
            val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
```

✅ 포인트

- Receiver는 트리거만 하고 실제 일은 Worker로 넘김

---

## 예제 2) 알림 액션 → Receiver에서 처리 후 앱 화면 이동(가벼운 처리)

```kotlin
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userId = intent.getLongExtra("user_id", -1L)

        val open = Intent(context, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("user_id", userId)
        }
        context.startActivity(open)
    }
}
```

✅ 포인트

- Receiver에서 Activity 실행 시 보통 `NEW_TASK`가 필요할 수 있음(문맥이 Activity가 아니므로)

---

## 예제 3) Dynamic Receiver 등록/해제(안전 패턴)

```kotlin
class MainActivity : AppCompatActivity() {

    private val airplaneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // 짧게 처리 (UI 갱신 정도)
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(
            airplaneReceiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(airplaneReceiver)
    }
}
```

---

# 1) PendingIntent?

## 핵심

- PendingIntent는 미래에 다른 주체(시스템/알림/다른 앱)가 내 앱의 Intent를 대신 실행할 수 있는 토큰
- Notification action은 보통 PendingIntent로 연결됨

> 쉽게 말하면: **지금 만든 Intent를 ‘나중에 시스템이 실행’하도록 맡기는 방식**
> 

---

# 2) Notification Action → BroadcastReceiver

## 왜 Receiver로 받나?

- 알림 버튼 클릭은 UI가 없을 수도 있고 곧바로 작업을 트리거하고 싶을 때가 많음
- 예: “좋아요”, “재시도”, “업로드 취소”, “로그아웃” 같은 빠른 액션

---

## 예제: 알림 버튼 클릭 → Receiver 수신 → 짧게 처리

### (1) Receiver

```kotlin
class UploadRetryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uploadId = intent.getLongExtra("upload_id", -1L)

        // onReceive에서는 짧게: WorkManager 재시도 예약
        val work = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(workDataOf("upload_id" to uploadId))
            .build()

        WorkManager.getInstance(context).enqueue(work)
    }
}
```

### (2) Notification Action에 PendingIntent 연결

```kotlin
val retryIntent = Intent(context, UploadRetryReceiver::class.java).apply {
    action = "ACTION_RETRY_UPLOAD"
    putExtra("upload_id", uploadId)
}

val pending = PendingIntent.getBroadcast(
    context,
    0,
    retryIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

// notification builder에 action으로 넣는다고 가정
// .addAction(icon, "Retry", pending)
```

✅ 포인트

- Receiver는 버튼 클릭 트리거를 받는 데 최적
- “실제 업로드”는 WorkManager로 넘겨서 보장/재시도/조건 처리

---

# 3) Receiver에서 Activity를 여는 패턴 (딥링크/화면 이동)

## 핵심

- Receiver는 Activity Context가 아니므로, Activity를 열 때 `FLAG_ACTIVITY_NEW_TASK`가 필요한 경우가 흔함
- 앱이 이미 열려 있는 경우까지 고려하면 launchMode/flags 설계가 중요

---

## 예제: Receiver → 특정 화면 열기

```kotlin
class OpenDetailReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userId = intent.getLongExtra("user_id", -1L)

        val open = Intent(context, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("user_id", userId)
        }
        context.startActivity(open)
    }
}
```

✅ 포인트

- `CLEAR_TOP`은 스택 정리 목적(상황에 따라 다름)
- 제대로 하려면 Activity 쪽 launchMode/Navigation 설계와 맞춰야 UX가 깔끔해진다.

---

# 4) Deep Link를 Receiver로 받을까? Navigation으로 받을까?

- 딥링크로 들어오면 화면만 열면 된다”→ 보통 Activity/Navigation 딥링크로 처리
- 딥링크 진입 전에 처리(검증/로그/조건)가 필요하거나 UI 없이 트리거만 필요 → Receiver가 유리할 때도 있음

> 결론: **화면 이동 중심이면 Navigation**, **트리거/백그라운드 작업 중심이면 Receiver+WorkManager** 쪽이 자연스러움
> 

---

# 5) 보안 심화: exported / Intent 검증 / PendingIntent 플래그

## 5-1) exported

- `exported=true`면 외부 앱이 내 Receiver를 호출할 수 있음
- 내부용이면 기본적으로 `exported=false` + 필요한 경우만 예외적으로 열기

---

## 5-2) Intent 검증 (입력값 신뢰 금지)

- `intent.action` 확인
- extras 존재/타입/범위 검증
- 민감 동작(결제, 삭제, 로그아웃)은 특히 강한 검증 필요

```kotlin
override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != "ACTION_RETRY_UPLOAD") return

    val id = intent.getLongExtra("upload_id", -1L)
    if (id <= 0) return

    // 안전하게 처리
}
```

---

## 5-3) PendingIntent FLAG (중요)

- `FLAG_IMMUTABLE`: PendingIntent 내용 변경 불가(보안 권장)
- `FLAG_MUTABLE`: 필요할 때만(특정 기능 요구 시)
- `FLAG_UPDATE_CURRENT`: extras 업데이트 목적


---

# 6) 백그라운드 제약과 정석 흐름

## 정석 패턴

- Receiver: 이벤트 수신(짧게)
- WorkManager: 조건/재시도/보장 실행 작업 담당
- Foreground Service: 지속 실행이 필요한 경우에만


---
