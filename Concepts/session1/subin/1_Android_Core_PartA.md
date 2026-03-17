# Android 스터디 1회차 — Part A

## Activity/Fragment Lifecycle · 데이터 보존 · Intent · Context · Navigation

## 전체 핵심 요약

- 생명주기(Lifecycle)는 **UI 생성/표시/중단/파괴**의 규칙이며, 데이터 보존 전략을 결정하는 기준이다.
- 데이터 보존은 “무엇이 사라졌는지”에 따라 전략이 달라진다.
    - **회전(재생성)** / **프로세스 죽음(Process Death)** / **백스택 이동**
- `Context`는 안드로이드 시스템/리소스에 접근하는 손잡이이며 종류별로 수명(lifetime)이 다르다.
- `Intent`는 컴포넌트 간 **메시지(이동/데이터 전달)** 이고, explicit/implicit과 extras로 나뉜다.
- `Navigation`은 화면 이동을 표준화하며, back stack과 생명주기 흐름을 일관되게 관리한다.

---

# 1) Activity 생명주기 (Lifecycle)

## 핵심

Activity는 대표적으로 아래 콜백 순서로 움직인다.

### 최초 진입(일반적)

- `onCreate()` → `onStart()` → `onResume()`

### 다른 화면으로 가려짐

- `onPause()` → (완전히 가려지면) `onStop()`

### 다시 돌아옴

- `onRestart()` → `onStart()` → `onResume()`

### 종료/제거

- `onPause()` → `onStop()` → `onDestroy()`

---

## 실무 포인트

### `onCreate()`

- 화면 초기화(뷰 inflate, DI, 초기 데이터 준비)
- **반복 호출 가능**: 회전/테마 변경/프로세스 복구에서 다시 호출됨

### `onResume()`

- 사용자가 실제로 인터랙션 가능한 상태
- 센서/카메라/위치 업데이트 등 “포그라운드만” 필요한 작업 시작 위치

### `onPause()/onStop()`

- 리소스 해제, 애니메이션 중지, 저장(필요 시)
- **짧고 안전하게**: `onPause()`는 매우 자주 호출됨

### `onDestroy()`

- 항상 호출되는 건 아님
    - 프로세스가 강제 종료되면 onDestroy 없이 사라질 수 있음

---

## ✅ 기술면접 체크(질문+답)

### Q1. `onCreate()`와 `onStart()`/`onResume()`의 차이는?

**A.**

- `onCreate()`는 “생성/초기 세팅” 단계(뷰 생성, 초기화).
- `onStart()`는 화면이 **보이기 시작**.
- `onResume()`는 사용자가 **상호작용 가능**(포그라운드).

### Q2. `onPause()`와 `onStop()` 차이는?

**A.**

- `onPause()`는 “포커스를 잃음”(부분 가림 포함).
- `onStop()`은 “완전히 안 보임”.
- UI 리소스 정리는 보통 `onStop()`까지 고려하지만, 민감한 자원은 `onPause()`에서 해제.

### Q3. `onDestroy()`는 항상 호출되나요?

**A.**

- 아니요. 프로세스 강제 종료/메모리 회수 등에서는 호출 없이 사라질 수 있음.

---

# 2) Fragment 생명주기 (Lifecycle)

## 핵심

Fragment는 Activity 생명주기에 더해 “View 생명주기”가 따로 있다.

- `onCreate()` : Fragment 자체 생성
- `onCreateView()` : View 생성
- `onViewCreated()` : View 준비 완료 (UI 작업 시작)
- `onStart()` → `onResume()`
- `onPause()` → `onStop()`
- `onDestroyView()` : **View만 파괴**
- `onDestroy()` : Fragment 객체 파괴

---

## 실무 포인트 (매우 중요)

- Fragment에서 UI 참조(ViewBinding 등)는 **`onDestroyView()`에서 정리**해야 안전함
- 코루틴/Flow collect도 가능하면 `viewLifecycleOwner` 기준으로 묶는다
    
    (뷰가 사라졌는데도 collect하면 크래시/누수 위험)
    

---

## ✅ 기술면접 체크(질문+답)

### Q4. Fragment에서 `onDestroy()`와 `onDestroyView()` 차이는?

**A.**

- `onDestroyView()`는 UI(View)가 파괴되는 시점. Fragment 객체는 남아 있을 수 있음.
- `onDestroy()`는 Fragment 자체가 완전히 제거되는 시점.
- 따라서 UI 참조는 `onDestroyView()`에서 null 처리하는 게 안전.

### Q5. Fragment에서 `viewLifecycleOwner`가 중요한 이유는?

**A.**

- Fragment의 View는 Fragment보다 먼저/자주 파괴될 수 있음.
- View 기준 lifecycle에 묶어야 “뷰 없는 상태에서 UI 업데이트”를 막을 수 있음.

---

# 3) 데이터 보존 방법 (회전/재생성/프로세스 죽음)

## 핵심 질문

“무엇이 사라지는 상황인가?”를 먼저 구분해야 함.

### A) 구성 변경(Configuration Change) — 예: 회전

- Activity/Fragment가 **재생성**될 수 있음
- `onCreate()` 다시 호출됨

### B) 프로세스 죽음(Process Death)

- 백그라운드에 있던 앱이 메모리 회수로 프로세스가 죽었다가, 돌아올 때 복구
- `savedInstanceState` 기반 복원이 핵심

### C) 화면 이동(Back stack)

- 화면을 떠났다가 돌아오는 상황 (onStop → onRestart 등)
- 완전한 “앱 종료”가 아님

---

## 데이터 보존 전략(여기서는 원칙만)

### 1) UI 입력값/간단 상태

- `onSaveInstanceState(Bundle)`
- 화면 재생성/프로세스 복구 시 복원에 유리

### 2) 화면 상태/비즈니스 데이터

- ViewModel로 관리하는 경우가 많음 (2회차/다음 파트에서 자세히)

### 3) 영구 저장이 필요한 데이터

- DB/SharedPreferences/파일 (이번 파트에서는 키워드만)

---

## ✅ 기술면접 체크(질문+답)

### Q6. 회전하면 Activity는 어떻게 되나요?

**A.**

- 기본적으로 Activity가 재생성될 수 있음 (`onDestroy` 후 새 `onCreate`).
- 따라서 “재생성 가능성”을 전제로 초기화/상태 복원 전략이 필요.

### Q7. 프로세스 죽음이랑 회전은 뭐가 다르죠?

**A.**

- 회전: 앱 프로세스는 살아있고 UI가 재생성되는 경우가 많음.
- 프로세스 죽음: 앱 프로세스 자체가 종료됐다가 복구 → 메모리에 있던 데이터가 사라짐.
- 그래서 프로세스 죽음 대비에는 `savedInstanceState`나 영구 저장/재요청 전략이 중요.

### Q8. `onSaveInstanceState`는 언제 호출되나요?

**A.**

- 시스템이 “나중에 복원할 가능성”을 대비할 때 호출될 수 있음.
- 하지만 항상 호출된다고 보장되진 않으므로, 중요한 데이터는 영구 저장/재생성 가능한 형태로 설계하는 게 안전.

---

# 4) Context 종류와 개념

## 핵심

`Context`는 **리소스 접근, 시스템 서비스 접근, 컴포넌트 실행**의 기반이 되는 “환경 정보”다.

---

## 대표 종류(실무에서 자주 구분)

### `Application Context`

- 앱 전체 수명과 함께 살아있음
- 전역적인 작업에 안전 (단, UI 작업엔 부적합)

### `Activity Context`

- Activity 수명과 함께
- UI 관련 작업에 적합 (Theme/Window 영향)

### `Fragment Context`

- 보통 `requireContext()` / `activity` 등을 통해 접근
- Fragment는 Activity에 붙어 있으므로 생명주기 타이밍 주의

---

## 실무 포인트(중요)

- **UI/Theme가 필요한 작업**은 Activity Context가 안전한 경우가 많음
- 오래 살아야 하는 객체(싱글톤/매니저)가 Activity Context를 잡으면 **메모리 누수 위험**이 커짐
    
    → 이런 경우 Application Context 고려
    

---

## ✅ 기술면접 체크(질문+답)

### Q9. Application Context vs Activity Context 차이는?

**A.**

- Application Context는 앱 프로세스 수명과 함께.
- Activity Context는 화면 수명과 함께.
- UI/Theme 관련 작업은 Activity Context가 필요할 수 있고, 전역 객체는 Activity Context를 오래 잡으면 누수 위험.

### Q10. “메모리 누수”와 Context가 왜 연결되나요?

**A.**

- 오래 살아야 하는 객체가 Activity Context를 참조하면, Activity가 끝나도 GC가 못 치우는 상황이 생길 수 있음.
- 그래서 전역 범위에는 Application Context를 쓰거나 참조를 끊는 설계가 필요.

---

# 5) Intent 개념과 사용

## 핵심

Intent는 컴포넌트 간 “요청/메시지”다.

- 화면 이동(Activity 시작)
- 서비스 시작
- 브로드캐스트 전송
- 데이터 전달(extras)

---

## Intent 종류

### Explicit Intent

- “어떤 컴포넌트를 실행할지” 명확히 지정

```kotlin
val intent = Intent(this, DetailActivity::class.java)
startActivity(intent)
```

### Implicit Intent

- “무슨 행동을 할지”만 지정하고, 처리 가능한 앱/컴포넌트에게 위임

```kotlin
val intent = Intent(Intent.ACTION_VIEW).apply {
    data = Uri.parse("https://www.google.com")
}
startActivity(intent)
```

---

## extras로 데이터 전달

```kotlin
val intent = Intent(this, DetailActivity::class.java).apply {
    putExtra("user_id", 1L)
}
startActivity(intent)
```

---

## ✅ 기술면접 체크(질문+답)

### Q11. Explicit vs Implicit Intent 차이?

**A.**

- Explicit: 대상 컴포넌트 지정(앱 내부 이동에 흔함).
- Implicit: 액션/데이터만 명시(브라우저 열기, 공유 등).

### Q12. Intent로 큰 객체를 넘기면 안 좋은 이유?

**A.**

- IPC/Bundle 크기 제한과 직렬화 비용 문제가 생길 수 있음.
- 보통 ID만 넘기고 실제 데이터는 재조회/공유 저장소를 사용.

---

# 6) Navigation (Jetpack Navigation)

## 핵심

Navigation은 화면 이동과 back stack을 표준화해서 관리하는 도구다.

- Fragment 이동을 안전하게
- 인자 전달(args) 패턴 제공
- back stack 관리 일관성

---

## 실무 포인트

- 화면 이동이 많아질수록 “직접 FragmentTransaction”보다 Navigation이 유지보수에 유리한 경우가 많음
- (심화) Safe Args, deep link, nested graph 등은 다음 회차로 확장 가능

---

## ✅ 기술면접 체크(질문+답)

### Q13. Navigation을 쓰는 이유는?

**A.**

- back stack/이동/인자 전달을 일관되게 관리해서 유지보수성과 안정성을 높이기 위해.

### Q14. FragmentTransaction 직접 vs Navigation 차이?

**A.**

- 직접 트랜잭션은 자유도가 높지만 규칙이 분산되기 쉬움.
- Navigation은 흐름을 graph로 관리해서 구조 파악/변경이 쉬워짐.

---
