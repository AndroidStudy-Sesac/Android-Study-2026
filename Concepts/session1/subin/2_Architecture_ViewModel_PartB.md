# Android 스터디 1회차 — Part B

## MVC/MVP/MVVM/MVI + ViewModel + LiveData + Observer

## 전체 핵심 요약

- 아키텍처 패턴은 “화면(UI)과 로직/데이터를 어떻게 분리하고 연결하느냐”의 규칙이다.
- Android 실무에서는 **MVVM + ViewModel**이 가장 보편적이고, 상황에 따라 **MVI**(단방향 상태)도 많이 쓴다.
- `ViewModel`은 화면 회전 등 구성 변경에도 데이터를 유지하며, UI 로직을 분리하는 핵심 컴포넌트다.
- `LiveData`는 lifecycle-aware observable로, UI가 살아있을 때만 안전하게 업데이트하도록 돕는다.
- Observer 패턴은 변화를 구독하고 반응하는 구조이며, LiveData/Flow/Compose State 모두 이 개념 위에 있다.

---

# 1) 패턴 개요: MVC / MVP / MVVM / MVI

## 1-1. 공통 목표

- UI 코드(Activity/Fragment/Compose)에서 **비즈니스 로직/상태 관리**를 분리
- 테스트 가능성, 유지보수성, 변경 용이성 향상

---

## 1-2. MVC (Model-View-Controller)

### 핵심

- Controller가 입력 처리 → Model 업데이트 → View 반영
- Android에서는 Activity/Fragment가 사실상 View+Controller 역할을 같이 하는 경우가 많아 **비대해지기 쉬움**

### 실무 포인트

- 작은 화면/간단 앱에서는 빠르게 만들기 좋지만, 규모 커지면 Activity/Fragment가 커짐

---

## 1-3. MVP (Model-View-Presenter)

### 핵심

- View는 “그리기만”
- Presenter가 View를 조작하는 로직 담당
- 보통 View는 interface로 정의하고 Presenter가 이를 호출

### 장점/단점

- 장점: View 테스트가 쉬워짐(인터페이스)
- 단점: View와 Presenter가 강하게 연결될 수 있음(참조/생명주기 관리 주의)

---

## 1-4. MVVM (Model-View-ViewModel) — 실무 메인

### 핵심

- ViewModel이 **UI 상태**를 가지고, View는 상태를 관찰(Observe)하여 그린다.
- ViewModel은 Activity/Fragment보다 오래 살아서 **회전(재생성)에도 데이터 유지**가 가능하다.

### 실무 포인트

- Android Jetpack의 ViewModel/LiveData/StateFlow와 궁합이 매우 좋음
- “화면 로직(상태)”을 ViewModel로 올리는 게 핵심

---

## 1-5. MVI (Model-View-Intent) — 단방향 상태 흐름

### 핵심

- **단방향 데이터 흐름(One-way data flow)**
    
    Intent(사용자 액션) → Reducer(상태 변경) → State(새 상태) → View 렌더링
    
- 상태가 `data class`로 표현되고 `copy()`로 업데이트되는 경우가 많음

### 실무 포인트

- 상태 변화 추적이 쉬움(디버깅/재현성)
- 패턴을 엄격히 지키면 구조가 안정적이지만 초기 러닝커브가 있을 수 있음

---

## ✅ 기술면접 체크(질문+답)

### Q1. MVC/MVP/MVVM/MVI의 차이를 한 줄씩 설명해보세요.

**A.**

- MVC: Controller가 입력 처리 후 Model을 바꾸고 View 반영
- MVP: Presenter가 View를 직접 업데이트(인터페이스로 View 제어)
- MVVM: ViewModel이 상태를 제공, View는 관찰해서 그린다
- MVI: Intent→Reducer→State→View로 단방향 흐름, 상태 중심

### Q2. Android에서 MVVM이 많이 쓰이는 이유는?

**A.**

- Jetpack ViewModel/Lifecycle/LiveData가 MVVM에 최적화되어 있고
- 화면 재생성에도 상태 유지가 쉬우며 테스트/분리가 상대적으로 깔끔하기 때문.

### Q3. MVI를 쓰면 뭐가 좋아지나요?

**A.**

- 상태 변경이 한 방향으로만 흘러서 예측 가능성이 높고
- “어떤 이벤트로 상태가 바뀌었는지” 추적이 쉬워짐.

---

# 2) ViewModel

## 핵심

- ViewModel은 UI에 필요한 데이터를 저장/관리하는 컴포넌트
- Activity/Fragment보다 **수명이 길어 구성 변경(Configuration Change)에도 데이터가 유지**될 수 있음
- UI 로직(상태 변경, 데이터 로딩)을 View에서 분리하는 역할

---

## Android 실무 예제 (MVVM 기본 형태)

### 2-1. ViewModel (상태 + 로딩)

```kotlin
class ProfileViewModel : ViewModel() {

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    fun loadProfile() {
        // 실무에서는 Repository 호출 (여기선 예시)
        _name.value = "Heewon"
    }
}
```

### 2-2. Fragment에서 관찰 + 렌더링

```kotlin
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val vm: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.name.observe(viewLifecycleOwner) { newName ->
            view.findViewById<TextView>(R.id.nameText).text = newName
        }

        vm.loadProfile()
    }
}
```

### 실무 포인트

- `viewLifecycleOwner`로 observe해야 View 생명주기와 맞아서 안전
- View는 “관찰하고 그리기”만 하고, 로직은 ViewModel로 이동

---

## ✅ 기술면접 체크(질문+답)

### Q4. ViewModel은 왜 필요하죠?

**A.**

- Activity/Fragment가 재생성(회전 등)될 때도 데이터를 유지할 수 있고,
- UI 로직을 분리해 테스트/유지보수를 쉽게 만들기 위해.

### Q5. ViewModel에 Context를 넣으면 안 되나요?

**A.**

- 일반적으로 지양. ViewModel은 UI보다 오래 살아서 Activity Context를 잡으면 누수 위험.
- 정말 필요하면 `AndroidViewModel(Application)`이나 DI로 ApplicationContext만 주입하는 방식이 있음(키워드).

### Q6. Fragment에서 `viewModels()`와 `activityViewModels()` 차이는?

**A.**

- `viewModels()`: Fragment 범위 ViewModel
- `activityViewModels()`: Activity 공유 범위 ViewModel(여러 Fragment가 공유)

---

# 3) LiveData

## 핵심

- `LiveData`는 lifecycle-aware observable
- UI가 활성 상태일 때만 값을 전달해 크래시/누수를 줄인다

---

## Android 실무 예제: 로딩/데이터/에러를 LiveData로 분리(기본)

```kotlin
class ListViewModel : ViewModel() {
    val loading = MutableLiveData(false)
    val items = MutableLiveData<List<String>>(emptyList())
    val error = MutableLiveData<String?>(null)

    fun load() {
        loading.value = true
        try {
            // 실제론 네트워크/DB 호출
            items.value = listOf("A", "B", "C")
        } catch (e: Exception) {
            error.value = e.message
        } finally {
            loading.value = false
        }
    }
}
```

Fragment:

```kotlin
vm.loading.observe(viewLifecycleOwner) { isLoading ->
    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
}

vm.items.observe(viewLifecycleOwner) { list ->
    adapter.submitList(list)
}

vm.error.observe(viewLifecycleOwner) { msg ->
    if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
```

---

## LiveData 실무 포인트

- UI가 사라지면 observe가 자동으로 비활성화되어 안전
- 다만 일회성 이벤트(토스트/네비게이션)를 LiveData로 하면 재관찰 시 재발생 문제가 생길 수 있음
    
    → 이벤트 래핑(Event wrapper) 또는 SharedFlow(2회차 코루틴 파트)로 해결 (키워드만)
    

---

## ✅ 기술면접 체크(질문+답)

### Q7. LiveData가 lifecycle-aware라는 게 무슨 뜻인가요?

**A.**

- Observer가 LifecycleOwner의 상태를 보고, STARTED/RESUMED 등 활성 상태에서만 값을 전달하는 것.

### Q8. LiveData는 왜 메모리 누수에 강한 편인가요?

**A.**

- LifecycleOwner가 파괴되면 observe가 자동 해제되기 때문에, UI가 사라진 뒤 업데이트하는 문제가 줄어듦.

### Q9. LiveData로 이벤트 처리 시 흔한 문제는?

**A.**

- 화면 회전/재구독 시 “마지막 값”이 다시 전달되어 토스트/네비게이션이 중복 실행될 수 있음.

---

# 4) Observer 패턴 (LiveData와의 연결)

## 핵심

Observer 패턴은 상태가 바뀌면 구독자에게 알린다는 구조

- Subject(발행자) → Observer(구독자)에게 변화 통지
- LiveData/Flow/Compose State 모두 이 개념 기반

---

## 실무 포인트

- UI는 데이터 변경을 “폴링”하지 않고 “구독”한다
- 구독 방식은 결합도를 낮추고 UI 업데이트를 자동화함

---

## ✅ 기술면접 체크(질문+답)

### Q10. Observer 패턴을 한 문장으로 설명해보세요.

**A.**

- 어떤 값(Subject)이 바뀌면 이를 관찰하는 Observer들에게 변경을 자동으로 알리는 패턴.

### Q11. Android에서 Observer 패턴의 대표 예시는?

**A.**

- LiveData observe, Flow collect, Compose state 관찰(재구성) 등이 대표적.

---

# 5) 패턴별 실무 선택 기준

## MVC

- 작은 화면/단순 앱엔 빠르지만 규모 커지면 Activity/Fragment가 비대해지기 쉬움

## MVP

- View 인터페이스로 테스트 용이성 확보 가능
- 하지만 View-Presenter 참조 관계/생명주기 관리가 복잡해질 수 있음

## MVVM (가장 흔함)

- ViewModel 중심으로 상태 관리 + LiveData/Flow로 UI 관찰
- Jetpack과 궁합 좋고 팀 채택률이 높음

## MVI

- 상태 변화가 예측 가능하고 디버깅이 편함
- 구조를 엄격히 잡으면 안정적이지만 초기 설계가 필요

---

# 6) 면접 대비: “1분 답변 템플릿”

## Q. “MVVM을 어떻게 구성하나요?”

**A.**

- View(Activity/Fragment/Compose)는 UI 렌더링과 사용자 입력 전달만 담당하고,
- ViewModel이 UI 상태를 관리하며,
- LiveData/StateFlow로 상태를 노출하고 View가 이를 관찰해 UI를 갱신합니다.
- 데이터 로딩은 Repository로 위임해 계층을 분리합니다.
