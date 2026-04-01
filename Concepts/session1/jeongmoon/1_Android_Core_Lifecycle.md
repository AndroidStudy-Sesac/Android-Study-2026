> ### 핵심 요약
> - 생명주기(Lifecycle): UI(화면)는 언제든 시스템에 의해 자비 없이 죽을 수 있는 불안정한 존재입니다.
> - 데이터 보존: 화면이 파괴될 때 데이터가 날아가지 않도록, ViewModel이라는 안전한 금고에 데이터를 대피시켜야 합니다.
> - Context (신분증): 화면과 같이 죽는 신분증(Activity)과 앱이 꺼질 때까지 살아있는 신분증(Application)을 혼동하면 치명적인 메모리 누수가 발생합니다.
> - Intent (메시지 봉투): 화면 간 이동 시 봉투에 너무 무거운 데이터를 욱여넣으면 앱이 터집니다. 가벼운 핵심 키(ID)만 전달하세요.


## 1. Activity & Fragment 생명주기 (Lifecycle) 완벽 비교
   
  > 🚨 안드로이드 화면은 언제든 시스템에 의해 죽을 수 있습니다.<br/>생명주기를 모르면 메모리가 새고(Memory Leak), 앱이 멈추며, 사용자 데이터가 날아갑니다.
  
  | **진행 단계 (상태)** | **Activity 생명주기** | **Fragment 생명주기** | **주요 특징 및 권장 작업** |
  | --- | --- | --- | --- |
  | **결합 및 초기화** | - | `onAttach()` `onCreate()` | Fragment가 Activity에 붙고 객체가 초기화됨 |
  | **UI 생성** | `onCreate()` | `onCreateView()` `onViewCreated()` | 레이아웃(XML) 연결, UI 컴포넌트(버튼 등) 초기화 및 리스너 세팅 |
  | **시작 (가시성 확보)** | `onStart()` | `onStart()` | 화면이 사용자에게 보이기 시작함 |
  | **활성화 (상호작용)** | **`onResume()`** | **`onResume()`** | **[앱 사용 중]** 포커스를 얻고 사용자와 상호작용(터치 등) 가능 |
  | **일시 정지 (포커스 잃음)** | `onPause()` | `onPause()` | 화면이 일부 가려지거나 포커스를 잃음 (진행 중인 작업 일시 정지) |
  | **정지 (화면 안 보임)** | `onStop()` | `onStop()` | 다른 화면으로 넘어가서 완전히 안 보임 (리소스 해제하기 좋은 시점) |
  | **UI 파괴** | - | `onDestroyView()` | Fragment의 화면(View)만 파괴됨 (객체는 살아있음) |
  | **완전 종료 및 분리** | `onDestroy()` | `onDestroy()` `onDetach()` | 메모리에서 객체가 완전히 파괴되고, Fragment는 Activity에서 떨어짐 |
  
  > - Fragment 생명주기 특징 <br/>
    Activity 위에 얹혀지는 조각이므로 Activity 생명주기에 종속됩니다.<br/>
    onAttach(), onCreateView()(UI 뷰 생성), onDestroyView() 등이 추가로 존재합니다.

💡 실무 팁
- `onPause()`에서 무거운 작업 금지: 이 단계가 빨리 끝나야 다음 화면이 뜹니다. 여기서 DB를 저장하거나 무거운 처리를 하면 앱 전환이 심각하게 버벅거립니다. <br/>
- `onDestroy()`는 안 불릴 수도 있다: 안드로이드 시스템이 메모리가 부족하면 앱을 강제 종료(Kill)하는데, 이때 `onDestroy()`를 호출할 여유조차 주지 않습니다. 반드시 저장해야 할 중요한 데이터라면 `onStop()`이나 그 이전에 저장하세요. <br/> 
- Fragment `onDestroyView()` 주의: 여기서 ViewBinding이나 DataBinding 변수를 null로 초기화해주지 않으면 100% 메모리 누수(Leak)가 발생합니다. (화면은 죽었는데 바인딩 객체가 계속 메모리를 파먹습니다.)

---

## 2. 데이터 보존 전략 (화면이 죽어도 데이터를 살려라)
- `ViewModel`: 화면이 회전해서 Activity가 파괴되어도 메모리에 살아남아 데이터를 쥐고 있는 아주 든든한 금고입니다.
- `savedInstanceState`: 시스템이 앱 프로세스 자체를 죽였을 때를 대비한 비상식량입니다. (Bundle 형태)

💡 실무 팁
- `ViewModel`에 `View`를 넣지 마세요: ViewModel 안에 Context나 TextView 같은 UI 객체를 들고 있으면 화면이 죽을 때 메모리에서 해제되지 않아 치명적인 에러가 납니다.
- `savedInstanceState`의 한계: 여기에 너무 큰 데이터(예: 고해상도 이미지 파일 전체, 수만 개의 리스트)를 넣으면 TransactionTooLargeException이 발생하며 앱이 터집니다. 50KB 이하의 가벼운 텍스트나 ID값만 넣으세요.

---

## 3. Context (문맥, 환경)
안드로이드 시스템의 리소스(문자열, 색상 등)를 가져오거나 권한을 확인할 때 내미는 '신분증'입니다.

💡 실무 팁
- `Context` 잘못 쓰면 앱 터집니다
  - `Activity Context`: 다이얼로그 띄울 때, UI 조작할 때. (화면 죽으면 같이 죽음)
  - `Application Context`: 싱글톤 객체나 앱 전역에서 쓸 때. (앱 꺼질 때까지 살아있음)
- 만약 앱 전체에서 쓰는 싱글톤 객체에 실수로 Activity Context를 넘겨주면? <br/>
  Activity가 종료되어도 싱글톤이 계속 붙잡고 있어서 화면이 메모리에서 지워지지 않습니다. 무조건 `applicationContext`를 쓰세요!

---

## 4. Intent (의도)
화면 간에 이동하거나 시스템에 무언가를 요청할 때 쓰는 '메시지 봉투'입니다.

💡 실무 팁
- `Intent`에 객체를 통째로 담지 마세요
  - A화면에서 B화면으로 넘어갈 때 Intent로 엄청나게 큰 사용자 객체(User Data)를 통째로 넘기는 분들이 많습니다. 이것도 앱이 느려지거나 터지는 주원인입니다.
- 해결책
  - 인텐트로는 유저의 고유 ID(문자열/숫자) 하나만 딸랑 넘기고, B화면이 열리면 그 ID를 가지고 ViewModel이나 DB에서 직접 데이터를 조회하게 만드는 것이 가장 안전합니다.
