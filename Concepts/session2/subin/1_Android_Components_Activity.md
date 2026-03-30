# Android 4대 컴포넌트 Part 1 — Activity

## 1) Activity가 하는 일 (정의/역할)

### 핵심 역할

- UI 표시: 화면 레이아웃 표시 및 렌더링
- 입력 처리: 클릭/터치/키 입력 등 사용자 이벤트 처리(대부분은 UI 계층에서)
- 화면 전환: 다른 Activity 시작, 결과 받기, back stack 관리
- 시스템 이벤트 대응: 구성 변경(회전) 백그라운드 전환 등 생명주기에 맞춰 리소스 관리

---

## 2) Activity 생명주기 (Lifecycle)

### 기본 흐름

- 시작: `onCreate → onStart → onResume`
- 가려짐: `onPause → onStop`
- 복귀: `onRestart → onStart → onResume`
- 종료: `onDestroy` (⚠️ 항상 호출 보장 X)

### 각 콜백에서 하는 일

### `onCreate()`

- 화면 초기 세팅(뷰 inflate, DI, observe 연결, 초기 로드 트리거)
- `savedInstanceState` 복원 지점(필요 시)

### `onStart()`

- 화면이 **보이기 시작**
- 가벼운 UI 갱신/리스너 등록 등 가능

### `onResume()`

- 사용자 **인터랙션 가능**
- 카메라/센서/위치 업데이트처럼 “포그라운드에서만” 필요한 것 시작

### `onPause()`

- 포커스 잃음(부분 가림 포함)
- **짧게**: 무거운 작업 금지(자주 호출됨)
- 민감한 자원(카메라/센서) 정리 시작점이 될 수 있음

### `onStop()`

- 완전히 안 보임
- 애니메이션/리소스 해제, UI 업데이트 중단, 관찰 중단(라이프사이클 기반이면 자동)

### `onDestroy()`

- 완전 종료 시점이지만 **프로세스 강제 종료/메모리 회수**로 호출 안 될 수 있음

---

## 3) Task / Back stack (중요)

### 핵심 개념

- **Task**: 사용자의 작업 흐름 단위(최근 앱 화면에서 보이는 단위)
- **Back stack**: 한 Task 안에서 Activity가 쌓이는 스택(LIFO)
- `startActivity()` 하면 보통 새 Activity가 스택 위로 올라감
- “뒤로가기”는 보통 스택 pop

### 실무에서 자주 나오는 것

- 로그인 → 메인 진입 후 뒤로가기 동작(로그인 화면으로 돌아가면 UX 이상)
- 딥링크로 특정 화면 진입 시 back stack 구성

---

## 4) Intent와 Activity의 관계

- Activity는 주로 `Intent`로 시작됨
    - Explicit: 앱 내부 화면 이동
    - Implicit: 외부 앱/공유/링크 등

**실무 팁**

- Intent로 큰 객체 전달 지양 → 보통 **ID만 전달**하고 상세 데이터는 로딩/공유 저장소로 해결

---

## 5) Android 실무 예제

### 예제 1) 화면 이동 + extras 전달 (기본)

```kotlin
val intent = Intent(this, DetailActivity::class.java).apply {
    putExtra("user_id", 1L)
}
startActivity(intent)
```

DetailActivity에서:

```kotlin
val userId = intent.getLongExtra("user_id", -1L)
```

### 예제 2) 로직은 ViewModel로 (Activity는 observe + render)

```kotlin
class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm.state.observe(this) { state ->
            render(state)
        }

        vm.load()
    }

    private fun render(state: UiState) {
        // UI만 업데이트
    }
}
```

---

## 6) 자주 하는 실수/주의점

- Activity에 네트워크/DB 로직 넣어서 비대해짐 → 테스트/유지보수 지옥
- `onPause()`에 무거운 작업 넣기 → 프리징/UX 저하
- Context 수명 문제: 전역 객체가 Activity Context 오래 잡으면 누수 위험
- 화면 회전(재생성) 대비 없이 “필드 변수”로 상태 유지 → 쉽게 사라짐

---

# 조금 더 심화내용

# 1) launchMode 개념 (manifest 속성)

## 1-1. launchMode가 하는 일

Activity를 실행할 때

- 새 인스턴스를 만들지,
- 기존 인스턴스를 재사용할지,
- 어느 task/back stack에 넣을지

를 기본 정책으로 정한다.

---

## 1-2. 4가지 launchMode

### A) `standard` (기본값)

- `startActivity()` 할 때마다 **무조건 새 인스턴스**
- back stack에 계속 쌓임

✅ 실무 감각: “대부분 화면은 standard”

---

### B) `singleTop`

- 스택 맨 위(top)에 **같은 Activity가 이미 있으면 재사용**
- 새 인스턴스 대신 `onNewIntent()` 호출

✅ 실무 감각:

- 같은 화면을 연속 클릭해서 중복으로 쌓이는 걸 막고 싶을 때
- “알림 클릭으로 같은 화면을 다시 열 때” 중복 방지 용도로 자주 씀

---

### C) `singleTask`

- task 안에 해당 Activity 인스턴스가 **1개만 존재**
- 이미 존재하면 그 위의 Activity들을 **pop(정리)** 하고, 해당 Activity로 이동
- `onNewIntent()`로 새 Intent 전달

✅ 실무 감각:

- “앱의 진입점(메인/홈)”을 한 번만 유지하고 싶을 때
- deep link로 들어와도 Home이 중복 생성되면 안 될 때

⚠️ 주의: 위에 있던 화면이 날아갈 수 있음 → UX 영향 큼

---

### D) `singleInstance`

- 해당 Activity가 **독립된 task**를 혼자 사용
- 다른 Activity와 같은 task에 섞이지 않음

✅ 실무 감각:

- 요즘 일반 앱에서 거의 안 씀
- 예전엔 특정 특수 화면(통화/카메라 느낌) 등에서 쓰는 경우가 있었음

---

## 1-3. `onNewIntent()`는 왜 중요할까?

`singleTop/singleTask/singleInstance`에서 “재사용”되는 경우, `onCreate()`가 아니라 `onNewIntent()`로 새 Intent가 들어온다.

```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    // 새로 들어온 intent의 extras 처리, 딥링크 처리 등
}
```

# 2) Intent Flags (코드로 동작 제어)

launchMode가 “기본 정책”이라면, flags는 실행 시점에 “이번 호출만 특별히” 제어하는 장치다.

---

## 2-1. 가장 자주 보는 flags

### A) `FLAG_ACTIVITY_NEW_TASK`

- 새로운 task에서 Activity를 시작
- Application Context에서 startActivity할 때 흔히 필요

✅ 실무 예:

- 알림 클릭(PendingIntent)이나, 앱 외부 진입 흐름에서 task를 새로 구성해야 할 때

---

### B) `FLAG_ACTIVITY_CLEAR_TOP`

- 스택에 해당 Activity가 이미 있으면, 그 위 Activity들을 제거하고 해당 Activity로 이동
- 보통 `singleTop`과 같이 쓰면, 기존 인스턴스를 살리고 `onNewIntent`로 받는 패턴이 잘 됨

✅ 실무 예:

- “홈으로 돌아가기” 버튼
- 딥링크가 들어왔을 때 기존 화면 정리하고 특정 화면으로 이동

---

### C) `FLAG_ACTIVITY_SINGLE_TOP`

- launchMode의 singleTop을 “이번 Intent에만” 적용하는 느낌

---

### D) `FLAG_ACTIVITY_CLEAR_TASK`

- task를 통째로 비움(보통 NEW_TASK와 함께)
- 로그인/로그아웃 플로우에서 많이 사용

✅ 실무 예:

- 로그인 성공 후, 로그인 화면 back stack을 완전히 없애기
- 로그아웃 후, 메인 화면 back stack을 완전히 없애기

---

## 2-2. 가장 흔한 조합: 로그인 성공 후 홈 진입

> 목표: 홈 들어간 다음 뒤로가기를 눌러도 로그인 화면으로 돌아가면 안 됨
> 

```kotlin
val intent = Intent(this, HomeActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}
startActivity(intent)
finish()
```

- `CLEAR_TASK`: 기존 스택 제거
- `NEW_TASK`: 새 task로 홈을 시작

---

## 2-3. 알림 클릭/딥링크 처리에서 흔한 조합

> 목표: 이미 앱이 떠 있는 경우 중복 인스턴스 생성/스택 꼬임 방지
> 

대표적으로:

- `singleTop` or `FLAG_ACTIVITY_SINGLE_TOP`
- `CLEAR_TOP`

# 3) Process Death 대응 포인트 (중요)

## 3-1. “onDestroy가 호출되면 저장하면 되지 않나요?” 

- 프로세스는 시스템에 의해 갑자기 죽을 수 있어 `onDestroy()`가 호출되지 않을 수 있음
- 따라서 process death 대비는
    - `onSaveInstanceState`
    - 재로딩 가능한 설계(서버/DB/Repo)
    - ViewModel + 저장소 조합
        
        으로 접근해야 안정적입니다.
        

---

## 3-2. 어떤 데이터가 사라지나?

### 프로세스 죽음 시 사라지는 것

- 메모리 상의 모든 상태(싱글톤/캐시/전역 변수 포함)
- ViewModel도 “프로세스와 함께” 사라질 수 있음

### 남을 수 있는 것

- `savedInstanceState`에 저장된 작은 UI 상태
- 영구 저장(SharedPreferences/DB/파일)

---

## 3-3. 실무 대응 전략

### 1) UI 입력/스크롤 위치 같은 “복원용 UI 상태”

- `onSaveInstanceState(Bundle)`에 저장
- 복원은 `onCreate(savedInstanceState)`에서

### 2) 중요한 데이터는 “재생성 가능하게”

- id만 저장하고 다시 로딩
- Repo가 DB/네트워크에서 복구 가능하게

### 3) 반드시 유지돼야 하는 사용자 입력은 영구 저장 고려

- 임시저장/드래프트 등

> ✅ 핵심: “저장 자체”보다 “복구 가능한 구조”가 중요
>
