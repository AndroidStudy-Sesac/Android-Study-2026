# 안드로이드 4대 컴포넌트

## 1. Activity

사용자에게 **화면(UI)을 보여주는 단위**

앱에서 사용자가 직접 상호작용하는 모든 화면이 Activity다. Compose를 사용하더라도 Activity는 여전히 필수 진입점이다.

### 생명주기

```
onCreate → onStart → onResume → [화면 표시 중]
         ↓
       onPause → onStop → onDestroy
```

| 콜백          | 호출 시점                      |
|-------------|----------------------------|
| `onCreate`  | Activity 최초 생성 시           |
| `onStart`   | 화면이 사용자에게 보이기 시작할 때        |
| `onResume`  | 포그라운드로 진입, 사용자 입력 받을 준비 완료 |
| `onPause`   | 다른 Activity가 앞에 올 때        |
| `onStop`    | 화면이 완전히 가려졌을 때             |
| `onDestroy` | Activity 종료 직전             |

### Compose에서의 사용 예시

```kotlin
// AndroidManifest.xml에 반드시 등록해야 함
// <activity android:name=".MainActivity" ...>

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Compose에서는 setContent 블록 안에 UI를 선언한다
        setContent {
            MyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    var count by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "클릭 횟수: $count")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { count++ }) {
            Text("클릭")
        }
    }
}
```

### Activity 간 이동

```kotlin
// 다른 Activity로 이동할 때는 Intent를 사용한다
@Composable
fun HomeScreen() {
    val context = LocalContext.current

    Button(onClick = {
        val intent = Intent(context, DetailActivity::class.java).apply {
            putExtra("itemId", 42)  // 데이터 전달
        }
        context.startActivity(intent)
    }) {
        Text("상세 화면으로 이동")
    }
}
```

- Compose를 쓰더라도 **Activity는 반드시 1개 이상 필요**하다
- **Single Activity + Compose Navigation** 패턴이 표준이다
- `ComponentActivity`를 상속하면 Compose와 Lifecycle을 함께 사용할 수 있다

---

# 2. Service

**백그라운드에서 작업을 수행**하는 컴포넌트

화면이 없고, 사용자 눈에 보이지 않는다. 음악 재생, 파일 다운로드, 위치 추적처럼 앱이 백그라운드에 있어도 계속 실행되어야 하는 작업에 사용한다.

---

## Service의 종류

| 종류                       | 설명                                     | 대표 사례            |
|--------------------------|----------------------------------------|------------------|
| **Started Service**      | `startService()`로 시작, 명시적으로 종료할 때까지 실행 | 파일 업로드           |
| **Bound Service**        | 컴포넌트와 바인딩, 바인딩이 해제되면 자동 종료             | 음악 플레이어 컨트롤      |
| **Foreground Service**   | 알림을 띄우며 실행, 시스템이 함부로 종료 못함             | 실시간 위치 추적, 음악 재생 |
| **혼합 (Started + Bound)** | 백그라운드 유지 + UI 제어를 동시에                  | 실제 음악 앱          |

---

## 코드 예시

### Started Service

`startService()`로 시작하고, 작업이 끝나면 스스로 `stopSelf()`를 호출해 종료하는 구조

```kotlin
// FileUploadService.kt
class FileUploadService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filePath = intent?.getStringExtra("file_path") ?: return START_NOT_STICKY

        // Service는 기본적으로 메인 스레드에서 실행되므로
        // 무거운 작업은 직접 스레드를 만들거나 Coroutine을 써야 한다
        CoroutineScope(Dispatchers.IO).launch {
            uploadFile(filePath)
            stopSelf()  // 작업 완료 후 스스로 종료
        }

        // START_NOT_STICKY: 시스템이 강제 종료하면 재시작하지 않음
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null  // Bound Service가 아니므로 null

    private suspend fun uploadFile(path: String) {
        // 실제 파일 업로드 로직
    }
}
```

```kotlin
// Compose에서 Started Service 시작
@Composable
fun UploadScreen() {
    val context = LocalContext.current

    Button(onClick = {
        val intent = Intent(context, FileUploadService::class.java).apply {
            putExtra("file_path", "/storage/emulated/0/photo.jpg")
        }
        context.startService(intent)
    }) {
        Text("파일 업로드 시작")
    }
}
```

**`onStartCommand` 반환값 비교**

| 반환값                      | 의미                            |
|--------------------------|-------------------------------|
| `START_STICKY`           | 강제 종료 후 재시작, Intent는 null로 전달 |
| `START_NOT_STICKY`       | 강제 종료 후 재시작하지 않음              |
| `START_REDELIVER_INTENT` | 강제 종료 후 재시작, 마지막 Intent 재전달   |

---

### Bound Service

컴포넌트(Activity, Fragment 등)가 **Service에 바인딩해서 직접 메서드를 호출**하는 구조

바인딩한 컴포넌트가 모두 없어지면 Service도 자동 종료

```kotlin
// MusicPlayerService.kt
class MusicPlayerService : Service() {

    // Binder를 통해 Service의 메서드를 외부에 노출한다
    inner class MusicBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    private val binder = MusicBinder()
    private var isPlaying = false

    // Bound Service의 핵심: onBind()에서 Binder를 반환해야 한다
    override fun onBind(intent: Intent): IBinder = binder

    // 외부에서 호출할 메서드들
    fun play() {
        isPlaying = true
        // 재생 로직
    }

    fun pause() {
        isPlaying = false
        // 일시정지 로직
    }

    fun isPlaying(): Boolean = isPlaying
}
```

```kotlin
// Compose에서 Bound Service 바인딩
@Composable
fun BoundMusicScreen() {
    val context = LocalContext.current
    var musicService by remember { mutableStateOf<MusicPlayerService?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // ServiceConnection: 바인딩 성공/해제 시 콜백
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                val musicBinder = binder as MusicPlayerService.MusicBinder
                musicService = musicBinder.getService()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                musicService = null
            }
        }
    }

    // DisposableEffect로 바인딩/언바인딩 생명주기 관리
    DisposableEffect(context) {
        val intent = Intent(context, MusicPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isPlaying) "재생 중" else "정지됨")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (isPlaying) {
                musicService?.pause()
            } else {
                musicService?.play()
            }
            isPlaying = !isPlaying
        }) {
            Text(if (isPlaying) "일시정지" else "재생")
        }
    }
}
```

---

### Foreground Service

알림을 띄우면서 실행되며, 시스템이 함부로 종료할 수 없다. 사용자가 인지해야 하는 장기 실행 작업에 사용한다.

```kotlin
// MusicService.kt
class MusicService : Service() {

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "music_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("음악 재생 중")
            .setContentText("My Playlist")
            .setSmallIcon(R.drawable.ic_music)
            .build()

        // Foreground Service로 전환 (알림 필수)
        startForeground(NOTIFICATION_ID, notification)

        startPlayback()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "음악 재생",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun startPlayback() { /* 음악 재생 */
    }
    private fun stopPlayback() { /* 재생 중지 */
    }
}
```

```kotlin
// Compose에서 Foreground Service 시작/종료
@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val intent = Intent(context, MusicService::class.java)
            context.startForegroundService(intent)  // Android 8.0 이상
        }) {
            Text("재생 시작")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val intent = Intent(context, MusicService::class.java)
            context.stopService(intent)
        }) {
            Text("재생 중지")
        }
    }
}
```

---

### Started + Bound 혼합

실제 음악 앱처럼 **백그라운드에서 계속 실행되면서, UI에서도 직접 제어**해야 하는 경우

`startService()`와 `bindService()`를 동시에 사용

바인딩이 모두 해제돼도 `startService()`로 시작된 상태라면 계속 실행됨

```kotlin
// FullMusicService.kt
class FullMusicService : Service() {

    inner class MusicBinder : Binder() {
        fun getService(): FullMusicService = this@FullMusicService
    }

    private val binder = MusicBinder()

    // onBind와 onStartCommand를 모두 구현하면 혼합 방식으로 동작한다
    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification())  // 백그라운드 유지를 위해 Foreground로 전환
        return START_STICKY
    }

    fun getCurrentTrack(): String = "현재 재생 중인 트랙"

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "channel_id")
            .setContentTitle("음악 재생 중")
            .setSmallIcon(R.drawable.ic_music)
            .build()
    }
}
```

```kotlin
// Compose에서 혼합 Service 사용
@Composable
fun FullMusicScreen() {
    val context = LocalContext.current
    var musicService by remember { mutableStateOf<FullMusicService?>(null) }
    var currentTrack by remember { mutableStateOf("") }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                musicService = (binder as FullMusicService.MusicBinder).getService()
                currentTrack = musicService?.getCurrentTrack() ?: ""
            }

            override fun onServiceDisconnected(name: ComponentName) {
                musicService = null
            }
        }
    }

    DisposableEffect(context) {
        // 1. startForegroundService: 백그라운드 유지용
        val intent = Intent(context, FullMusicService::class.java)
        context.startForegroundService(intent)

        // 2. bindService: UI 제어용
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(serviceConnection)
            // Service는 unbind해도 start된 상태라면 계속 실행된다
            // 완전히 종료하려면 stopService()를 별도로 호출해야 한다
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("현재 트랙: $currentTrack")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            context.stopService(Intent(context, FullMusicService::class.java))
        }) {
            Text("완전 종료")
        }
    }
}
```

---

## Service 종류

| 종류             | 시작 방법                      | 종료 조건                           | `onBind` 반환 | 대표 사례         |
|----------------|----------------------------|---------------------------------|-------------|---------------|
| **Started**    | `startService()`           | `stopSelf()` 또는 `stopService()` | `null`      | 파일 업로드        |
| **Bound**      | `bindService()`            | 모든 바인딩 해제 시 자동                  | `IBinder`   | 음악 컨트롤 UI     |
| **Foreground** | `startForegroundService()` | 명시적 종료 또는 `stopSelf()`          | 선택          | 실시간 위치, 음악 재생 |
| **혼합**         | 둘 다                        | 바인딩 해제 + 명시적 종료 모두 충족           | `IBinder`   | 실제 음악 앱       |

---

- Android 8.0(Oreo) 이상부터 **백그라운드 Service 실행에 제한**이 생겼다. 백그라운드에서 실행하려면 **Foreground Service**를 사용하고 알림을 반드시 표시해야 한다.
- Service는 기본적으로 **메인 스레드에서 실행**된다. 무거운 작업은 내부에서 Coroutine을 직접 써야 한다.
- 단발성 비동기 작업은 **Coroutine**, 앱 종료 후에도 보장이 필요한 작업은 **WorkManager**가 더 적합하다. Service는 장기 지속되면서 사용자가 인지해야 하는 작업에만 사용하는 것이
  좋다.

---

# 3. BroadcastReceiver

**시스템 또는 앱에서 발생하는 이벤트(브로드캐스트)를 수신**하는 컴포넌트

배터리 부족, 네트워크 연결 상태 변경, 화면 켜짐/꺼짐 등의 시스템 이벤트를 감지할 때 사용

---

## 등록 방법 2가지

| 방법        | 특징                  | 등록 위치                 |
|-----------|---------------------|-----------------------|
| **정적 등록** | 앱이 실행 중이 아니어도 수신 가능 | `AndroidManifest.xml` |
| **동적 등록** | 컴포넌트 생명주기에 따라 등록/해제 | 코틀린 코드                |

### Android 8.0 이후 정적 등록 제한

보안과 배터리 효율 문제로, Android 8.0(Oreo)부터 **정적 등록으로 수신할 수 있는 브로드캐스트가 대폭 제한**되었음

| 대표적인 허용 브로드캐스트              | 설명          |
|-----------------------------|-------------|
| `BOOT_COMPLETED`            | 기기 부팅 완료    |
| `MY_PACKAGE_REPLACED`       | 내 앱 업데이트 완료 |
| `ACTION_POWER_CONNECTED`    | 충전기 연결      |
| `ACTION_POWER_DISCONNECTED` | 충전기 분리      |

네트워크 상태 변경(`CONNECTIVITY_ACTION`) 같은 브로드캐스트는 8.0부터 정적 등록으로 받을 수 없다. 이런 경우에는 동적 등록을 사용해야 한다.
> 허용된 전체 목록은 [암묵적 브로드캐스트 예외 목록](https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions) 공식 문서를 참고
---

## 코드 예시

### 정적 등록: 부팅 완료 감지

앱이 실행 중이 아니어도 기기가 부팅되면 수신할 수 있다. `AndroidManifest.xml`에 등록하고, 수신 시 실행할 리시버 클래스를 코틀린으로 작성한다.

```xml
<!-- AndroidManifest.xml -->
<!-- 권한 선언 -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>

<application ...>
<receiver
android:name=".BootReceiver"
android:exported="true">  <!-- 시스템 브로드캐스트 수신 시 true 필요 -->
<intent-filter>
    <action android:name="android.intent.action.BOOT_COMPLETED"/>
</intent-filter>
</receiver>
        </application>
```

```kotlin
// BootReceiver.kt
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 부팅 완료 시 실행할 작업
            // 예: 알람 재등록, WorkManager 작업 스케줄링 등
            scheduleAlarm(context)
        }
    }

    private fun scheduleAlarm(context: Context) {
        // 알람 재등록 로직
        // 기기가 재부팅되면 AlarmManager에 등록된 알람이 모두 사라지므로
        // BOOT_COMPLETED에서 다시 등록해주는 것이 대표적인 패턴이다
    }
}
```

> `onReceive()`는 **메인 스레드**에서 실행되며 최대 10초 제한이 있다. 무거운 작업은 `onReceive()` 안에서 직접 처리하지 말고, WorkManager나 Service에 위임해야 한다.

---

### 동적 등록: 네트워크 상태 감지

코드에서 직접 등록하고 해제한다. Compose에서는 **`DisposableEffect`** 를 사용해 Composable 생명주기에 맞춰 자동으로 등록/해제할 수 있다.

```kotlin
// NetworkReceiver.kt
class NetworkReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isConnected = connectivityManager.activeNetworkInfo?.isConnected == true

            if (isConnected) {
                Toast.makeText(context, "네트워크 연결됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "네트워크 연결 끊김", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

```kotlin
// Compose에서 동적으로 BroadcastReceiver 등록/해제
@Composable
fun NetworkAwareScreen() {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }

    // DisposableEffect: Composable이 화면에 나타날 때 등록,
    // 사라질 때 onDispose 블록이 실행되어 자동 해제된다
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val connectivityManager =
                    ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                isConnected = connectivityManager.activeNetworkInfo?.isConnected == true
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isConnected) {
            Text("온라인 상태입니다", color = Color.Green)
        } else {
            Text("오프라인 상태입니다", color = Color.Red)
        }
    }
}
```

---

### 커스텀 브로드캐스트

시스템 이벤트 외에도 **앱 내부 또는 앱 간에 직접 이벤트를 발송**할 수 있다.

```kotlin
// 브로드캐스트 발송
@Composable
fun BroadcastDemoScreen() {
    val context = LocalContext.current

    Button(onClick = {
        val intent = Intent("com.example.MY_CUSTOM_ACTION").apply {
            putExtra("message", "커스텀 이벤트 발생!")
        }
        context.sendBroadcast(intent)
    }) {
        Text("브로드캐스트 발송")
    }
}
```

```kotlin
// 커스텀 브로드캐스트 수신 (동적 등록)
DisposableEffect(context) {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val message = intent.getStringExtra("message")
            Log.d("BroadcastDemo", "수신: $message")
        }
    }

    val filter = IntentFilter("com.example.MY_CUSTOM_ACTION")
    context.registerReceiver(receiver, filter)

    onDispose {
        context.unregisterReceiver(receiver)
    }
}
```

> 앱 내부에서만 브로드캐스트를 주고받는다면 `sendBroadcast()` 대신 **`LocalBroadcastManager`** 를 사용하는 것이 보안상 더 안전하다.
> 하지만 `LocalBroadcastManager`도 deprecated 되는 추세라, 앱 내부 이벤트는 **StateFlow나 SharedFlow**로 대체하는 것을 권장

---

## 정적 등록 vs 동적 등록 비교

|                       | 정적 등록                 | 동적 등록             |
|-----------------------|-----------------------|-------------------|
| **등록 위치**             | `AndroidManifest.xml` | 코틀린 코드            |
| **앱 미실행 시 수신**        | O                     | X                 |
| **Android 8.0 이후 제한** | 대부분의 시스템 브로드캐스트 수신 불가 | 제한 없음             |
| **생명주기 관리**           | 시스템이 관리               | 직접 등록/해제 필요       |
| **대표 사례**             | 부팅 완료, 앱 업데이트         | 네트워크 상태, 화면 켜짐/꺼짐 |

---

- Compose에서는 **`DisposableEffect`** 를 사용해 생명주기에 맞게 등록/해제하는 것이 안전하다.
- `onReceive()`는 **메인 스레드**에서 실행되므로 무거운 작업 금지. 최대 10초 제한이 있으며, 초과 시 ANR이 발생한다.
- Android 8.0 이상에서는 **정적 등록 가능한 브로드캐스트가 제한**되므로, 대부분의 경우 동적 등록을 사용해야 한다.
- 앱 내부 이벤트 전달은 BroadcastReceiver보다 **StateFlow / SharedFlow**가 더 적합하다.

---

## 4. ContentProvider

### 개념

**앱 간에 데이터를 공유**할 수 있게 해주는 컴포넌트

연락처, 갤러리, 캘린더 등 시스템 앱의 데이터에 접근할 때 ContentProvider를 통한다. URI를 기반으로 데이터를 CRUD(
생성/조회/수정/삭제)한다.

### URI 구조

```
content://com.example.provider/users/1
         └──────────────────┘ └───┘ └┘
              authority       경로   ID
```

### 코드 예시: 연락처 읽기

앱에서 ContentProvider에 직접 접근할 때는 `ContentResolver`를 사용

```kotlin
// ContactsViewModel.kt
class ContactsViewModel : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    fun loadContacts(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val contactList = mutableListOf<Contact>()

            // ContentResolver로 연락처 ContentProvider에 접근
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  // URI
                arrayOf(                                               // 가져올 컬럼
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,   // WHERE 조건
                null,   // WHERE 파라미터
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"  // 정렬
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
                val numberIndex = it.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                while (it.moveToNext()) {
                    contactList.add(
                        Contact(
                            name = it.getString(nameIndex),
                            phone = it.getString(numberIndex)
                        )
                    )
                }
            }

            _contacts.value = contactList
        }
    }
}

data class Contact(val name: String, val phone: String)
```

```kotlin
// Compose UI에서 연락처 목록 표시
@Composable
fun ContactListScreen(viewModel: ContactsViewModel = viewModel()) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsState()

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadContacts(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(contacts) { contact ->
            ListItem(
                headlineContent = { Text(contact.name) },
                supportingContent = { Text(contact.phone) }
            )
            HorizontalDivider()
        }
    }
}
```

### 커스텀 ContentProvider 만들기

```kotlin
// 자신의 앱 데이터를 외부에 공유할 때 ContentProvider를 직접 구현해야함
class UserContentProvider : ContentProvider() {

    private lateinit var database: UserDatabase

    override fun onCreate(): Boolean {
        database = UserDatabase.getInstance(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return database.userDao().getAllUsersCursor()
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // 데이터 삽입 로직
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun getType(uri: Uri): String? = null
}
```

- 직접 ContentProvider를 만들 일은 많지 않지만, **시스템 데이터(연락처, 미디어 등)에 접근**할 때는 반드시 알아야 됨
- 데이터 접근 전 **권한(Permission) 요청**을 해야됨
- `Cursor`는 반드시 **`use { }` 블록**으로 감싸 자동으로 닫히게 해야 한다

---

## 전체 비교 요약

| 컴포넌트                  | 역할         | UI 여부 | 대표 사례          |
|-----------------------|------------|-------|----------------|
| **Activity**          | 화면 단위      | O     | 홈 화면, 설정 화면    |
| **Service**           | 백그라운드 작업   | X     | 음악 재생, 파일 다운로드 |
| **BroadcastReceiver** | 이벤트 수신     | X     | 네트워크 변경 감지, 알람 |
| **ContentProvider**   | 앱 간 데이터 공유 | X     | 연락처 조회, 갤러리 접근 |

---

## Manifest 등록 요약

4대 컴포넌트는 모두 `AndroidManifest.xml`에 등록해야 시스템이 인식한다. (단, BroadcastReceiver는 동적 등록 시 불필요)

```xml

<application ...>

        <!-- Activity -->
<activity android:name=".MainActivity">
<intent-filter>
    <action android:name="android.intent.action.MAIN"/>
    <category android:name="android.intent.category.LAUNCHER"/>
</intent-filter>
</activity>

        <!-- Service -->
<service
android:name=".MusicService"
android:foregroundServiceType="mediaPlayback"/>

        <!-- BroadcastReceiver (정적 등록) -->
<receiver android:name=".BootReceiver">
<intent-filter>
    <action android:name="android.intent.action.BOOT_COMPLETED"/>
</intent-filter>
</receiver>

        <!-- ContentProvider -->
<provider
android:name=".UserContentProvider"
android:authorities="com.example.provider"
android:exported="false"/>

        </application>
```