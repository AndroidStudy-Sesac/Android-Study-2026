# Jetpack Compose 완전 정리

---

## Compose란?

Jetpack Compose는 Android의 **선언형 UI 프레임워크**. 기존의 XML + View 방식 대신 Kotlin 코드로만 UI를 작성.

### 명령형 vs 선언형 UI

| | 명령형 (기존 View) | 선언형 (Compose) |
|--|------------------|-----------------|
| 방식 | UI를 직접 조작 (`view.text = "..."`) | 상태에 따라 UI가 자동으로 결정됨 |
| 관심사 | "어떻게 변경할까?" | "어떤 상태일 때 어떻게 보여야 하는가?" |
| 코드 위치 | XML + Kotlin | Kotlin만 |

---

## 1. State (상태 관리)

### 개념
Compose에서 UI는 **State(상태)의 함수**. 상태가 바뀌면 관련 Composable이 자동으로 재구성(Recomposition).

```
UI = f(State)
```

### remember
- **현재 Composition 내에서** 값을 기억
- 화면 회전 시 초기화됨

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }

    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

### rememberSaveable
- 화면 회전, 프로세스 종료 후 복원에도 값 유지
- `Bundle`에 저장 가능한 타입만 자동 지원 (커스텀 타입은 `Saver` 필요)

```kotlin
@Composable
fun SearchBar() {
    var query by rememberSaveable { mutableStateOf("") }
    // 화면 회전 후에도 query 유지 ✅

    TextField(value = query, onValueChange = { query = it })
}
```

### remember vs rememberSaveable 비교

| | remember | rememberSaveable |
|--|----------|-----------------|
| 저장 위치 | Composition 메모리 | Bundle (Activity 상태 저장) |
| 화면 회전 후 | ❌ 초기화 | ✅ 유지 |
| 언제 사용? | 일시적 UI 상태 | 사용자 입력, 중요한 UI 상태 |

### MutableState 생성 방법
```kotlin
// 두 방식은 동일, by 위임을 쓰면 .value 생략 가능
val count: MutableState<Int> = remember { mutableStateOf(0) }
count.value++

var count by remember { mutableStateOf(0) }  // 권장
count++  // .value 생략
```

---

## 2. Recomposition (재구성)

### 개념
상태가 변경되면 해당 상태를 읽는 Composable 함수만 **다시 실행**되는 과정.  
전체 UI를 다시 그리는 게 아니라 변경이 필요한 부분만 재구성.

### 동작 원리
```kotlin
@Composable
fun Screen() {
    var name by remember { mutableStateOf("Android") }

    Header()           // name 안 읽으면 재구성 안 됨 ✅
    GreetingText(name) // name 읽으므로 name 변경 시 재구성 됨 🔄
    Footer()           // name 안 읽으면 재구성 안 됨 ✅
}
```

### Recomposition 최적화 팁

```kotlin
// ❌ 나쁜 예: 람다 안에서 상태를 직접 읽으면 매번 재구성
@Composable
fun BadExample(list: List<Item>) {
    list.forEach { item ->
        ItemRow(item)  // list 변경 시 모든 ItemRow 재구성
    }
}

// ✅ 좋은 예: key를 사용해 변경된 항목만 재구성
@Composable
fun GoodExample(list: List<Item>) {
    list.forEach { item ->
        key(item.id) {
            ItemRow(item)  // 해당 item만 재구성
        }
    }
}
```

---

## 3. State Hoisting (상태 호이스팅)

### 개념
Composable을 **Stateless(상태 없음)** 하게 만들기 위해 상태를 **부모로 끌어올리는** 패턴.

### 왜 필요한가?
- 상태가 내부에 있으면 외부에서 제어 불가 → 재사용성 낮음
- 상태를 부모로 올리면 → 테스트 용이, 재사용 가능, 단일 진실의 원천(SSOT) 유지

### Before (Stateful - 나쁜 예)
```kotlin
@Composable
fun CounterButton() {
    var count by remember { mutableStateOf(0) }  // 내부에 상태 존재
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
    // 외부에서 count를 읽거나 초기화할 수 없음 ❌
}
```

### After (Stateless + Hoisting - 좋은 예)
```kotlin
// Stateless: 상태 없이 값과 이벤트만 받음
@Composable
fun CounterButton(
    count: Int,             // 상태를 부모로부터 받음
    onIncrement: () -> Unit // 이벤트를 부모로 전달
) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}

// 부모가 상태를 소유
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }

    CounterButton(
        count = count,
        onIncrement = { count++ }
    )
}
```

### State Hoisting 패턴 요약
```
부모 Composable
  - 상태(State) 소유
  - 이벤트 핸들러 정의
  ↓ (state, onEvent) 전달
자식 Composable (Stateless)
  - 받은 state로 UI 렌더링
  - 사용자 입력 시 onEvent 호출
```

---

## 4. Side-effects (부수 효과)

### 개념
Composable 함수 외부의 상태를 변경하거나, 비동기 작업을 실행하는 것.  
Compose에서는 **Recomposition에 안전하게** 부수 효과를 처리하기 위한 API를 제공.

### LaunchedEffect
- Composable이 처음 구성될 때 또는 key가 변경될 때 **코루틴 실행**
- Composable이 사라지면 코루틴 자동 취소

```kotlin
@Composable
fun UserProfile(userId: String) {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(userId) {  // userId가 바뀌면 재실행
        user = userRepository.getUser(userId)
    }

    user?.let { Text(it.name) }
}
```

### SideEffect
- **매 Recomposition마다** 실행 (성공적인 recomposition 이후)
- Compose 외부의 객체에 상태를 동기화할 때

```kotlin
@Composable
fun AnalyticsScreen(screenName: String) {
    SideEffect {
        analytics.setCurrentScreen(screenName) // 매 recomposition마다 동기화
    }
}
```

### DisposableEffect
- Composable이 등장할 때 setup, 사라질 때 cleanup 처리

```kotlin
@Composable
fun LocationScreen() {
    DisposableEffect(Unit) {
        val listener = LocationListener { ... }
        locationManager.startUpdates(listener)

        onDispose {
            locationManager.stopUpdates(listener) // 정리
        }
    }
}
```

### rememberCoroutineScope
- Composable 내에서 사용자 이벤트 등 **명시적으로 코루틴 실행**할 때

```kotlin
@Composable
fun SaveButton(data: String) {
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            saveData(data)  // 버튼 클릭 시 코루틴 실행
        }
    }) {
        Text("Save")
    }
}
```

### produceState
- 비동기 데이터를 State로 변환

```kotlin
@Composable
fun UserName(userId: String): State<String> {
    return produceState(initialValue = "Loading...") {
        value = userRepository.getUserName(userId)
    }
}
```

---

## 5. Modifier

### 개념
Composable의 **크기, 레이아웃, 외형, 동작을 설정**하는 객체.  
Compose에서 UI를 꾸미는 방법은 대부분 Modifier를 통해 이루어짐.

### 왜 Modifier인가?
기존 View는 XML 속성으로 스타일/레이아웃을 설정했지만, Compose는 모든 것을 **코드 체이닝**으로 처리. 일관된 방식으로 UI를 제어할 수 있음.

### 기본 사용법
```kotlin
Text(
    text = "Hello",
    modifier = Modifier
        .fillMaxWidth()          // 가로 최대
        .padding(16.dp)          // 안쪽 여백
        .background(Color.Blue)  // 배경색
        .clickable { }           // 클릭 처리
)
```

### ⚠️ Modifier 순서가 중요!
```kotlin
// padding → background: 배경이 padding 안쪽에만 적용
Modifier.padding(16.dp).background(Color.Blue)

// background → padding: 배경이 padding 포함해서 적용
Modifier.background(Color.Blue).padding(16.dp)
```

### 자주 사용하는 Modifier 카테고리

#### 크기 관련
```kotlin
Modifier.fillMaxSize()         // 부모 전체 크기
Modifier.fillMaxWidth()        // 가로만 최대
Modifier.fillMaxHeight()       // 세로만 최대
Modifier.size(100.dp)          // 고정 크기
Modifier.width(100.dp)         // 가로만 고정
Modifier.height(50.dp)         // 세로만 고정
Modifier.wrapContentSize()     // 내용 크기에 맞춤
Modifier.weight(1f)            // Row/Column 내 비율 배분
```

#### 여백 관련
```kotlin
Modifier.padding(16.dp)                        // 모든 방향
Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
Modifier.padding(start = 8.dp, end = 8.dp)
// ⚠️ Compose에는 margin 개념 없음. padding으로 대체
```

#### 배경 / 모양
```kotlin
Modifier.background(Color.Blue)
Modifier.background(
    color = Color.Blue,
    shape = RoundedCornerShape(8.dp)
)
Modifier.clip(RoundedCornerShape(8.dp))        // 모양대로 잘라냄
Modifier.border(1.dp, Color.Black, RoundedCornerShape(8.dp))
Modifier.shadow(elevation = 4.dp)
```

#### 정렬 / 레이아웃
```kotlin
Modifier.align(Alignment.CenterHorizontally)  // Column 내 가로 중앙
Modifier.align(Alignment.Center)              // Box 내 중앙
Modifier.offset(x = 10.dp, y = 5.dp)         // 위치 이동
```

#### 상호작용
```kotlin
Modifier.clickable { /* 클릭 */ }
Modifier.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null  // 리플 효과 제거
) { }
Modifier.combinedClickable(
    onClick = { },
    onLongClick = { }
)
Modifier.pointerInput(Unit) { detectTapGestures(...) }
```

---

## 6. Theme (테마)

### 개념
앱 전체의 **색상, 타이포그래피, 모양**을 일관되게 관리하는 시스템. `MaterialTheme`을 루트에 적용.

```kotlin
@Composable
fun MyApp() {
    MyAppTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen()
        }
    }
}
```

### Color Scheme (Material 3)
```kotlin
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// 사용
Text(
    text = "Hello",
    color = MaterialTheme.colorScheme.primary
)
```

---

## 7. Layout (레이아웃)

### 기본 레이아웃 컴포넌트

```kotlin
// Column: 세로 정렬
Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text("Top")
    Text("Middle")
    Text("Bottom")
}

// Row: 가로 정렬
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(...)
    Text("Label")
}

// Box: 겹침 레이아웃 (FrameLayout과 유사)
Box(modifier = Modifier.fillMaxSize()) {
    Image(modifier = Modifier.fillMaxSize(), ...)
    Text(
        modifier = Modifier.align(Alignment.BottomCenter),
        text = "Overlay Text"
    )
}
```

### List (목록)

```kotlin
// LazyColumn: 세로 스크롤 목록 (RecyclerView 대체)
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(
        items = userList,
        key = { user -> user.id }  // 성능 최적화를 위해 key 지정
    ) { user ->
        UserCard(user)
    }

    item { Divider() }  // 단일 아이템 추가

    itemsIndexed(items) { index, item ->  // 인덱스 필요할 때
        Text("$index: ${item.name}")
    }
}

// LazyRow: 가로 스크롤 목록
LazyRow { ... }

// LazyVerticalGrid: 격자 목록
LazyVerticalGrid(columns = GridCells.Fixed(2)) { ... }
```

---

## 8. Gestures & Animation (제스처 & 애니메이션)

### Gestures

```kotlin
// 스와이프 감지
val offsetX = remember { Animatable(0f) }

Box(
    Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures { _, dragAmount ->
            // dragAmount: 드래그 양
        }
    }
)

// 확대/축소 (핀치 줌)
var scale by remember { mutableStateOf(1f) }
Image(
    modifier = Modifier
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .pointerInput(Unit) {
            detectTransformGestures { _, _, zoom, _ ->
                scale *= zoom
            }
        }
)
```

### Animation

```kotlin
// 1. animateFloatAsState: 단순 값 애니메이션
val alpha by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0f,
    animationSpec = tween(durationMillis = 300),
    label = "alpha"
)
Box(Modifier.alpha(alpha)) { ... }

// 2. AnimatedVisibility: 나타남/사라짐 애니메이션
AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut()
) {
    ExpandedContent()
}

// 3. animateContentSize: 크기 변경 애니메이션
Box(
    Modifier.animateContentSize(
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
) {
    if (isExpanded) ExpandedContent() else CollapsedContent()
}

// 4. Crossfade: 두 콘텐츠 전환
Crossfade(targetState = isLoggedIn, label = "login") { loggedIn ->
    if (loggedIn) HomeScreen() else LoginScreen()
}
```

---

## 9. CompositionLocal

### 개념
Composable 계층에서 **명시적으로 파라미터를 전달하지 않고** 데이터를 하위 Composable에 전달하는 방법.

### 왜 사용?
- Context, Theme, 사용자 설정 등을 모든 하위 Composable에 파라미터로 전달하면 매우 번거로움
- CompositionLocal을 사용하면 **자동으로 하위 트리에 값이 전파**됨

### 내장 CompositionLocal
```kotlin
// 이미 Compose에서 제공하는 것들
LocalContext.current          // Context
LocalLifecycleOwner.current   // LifecycleOwner
MaterialTheme.colorScheme     // 내부적으로 CompositionLocal 사용
```

### 커스텀 CompositionLocal 생성
```kotlin
// 1. 정의
val LocalUserName = compositionLocalOf<String> {
    error("No user name provided") // 기본값 없음 (필수 제공)
}

val LocalTextSize = staticCompositionLocalOf { 14.sp } // 정적 기본값

// 2. 값 제공 (CompositionLocalProvider)
@Composable
fun App() {
    CompositionLocalProvider(
        LocalUserName provides "홍길동",
        LocalTextSize provides 16.sp
    ) {
        MainScreen() // 하위 트리에서 접근 가능
    }
}

// 3. 하위 Composable에서 사용
@Composable
fun UserGreeting() {
    val userName = LocalUserName.current
    val textSize = LocalTextSize.current
    Text("안녕하세요, $userName!", fontSize = textSize)
}
```

### compositionLocalOf vs staticCompositionLocalOf

| | compositionLocalOf | staticCompositionLocalOf |
|--|-------------------|--------------------------|
| 값 변경 시 | 해당 값을 읽는 Composable만 재구성 | **전체 CompositionLocalProvider 하위 재구성** |
| 적합한 경우 | 자주 바뀌는 값 (사용자 설정 등) | 거의 안 바뀌는 값 (테마, 설정 등) |
