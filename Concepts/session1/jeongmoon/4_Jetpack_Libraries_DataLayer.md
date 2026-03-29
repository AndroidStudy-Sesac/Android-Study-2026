> ### 핵심 요약
> - 상태 관리: ViewModel이 데이터를 들고 있고, StateFlow(또는 LiveData)가 UI에 알림을 쏩니다.
> - 네비게이션 (Navigation Compose): XML 파일 없이 오직 코틀린 코드로 화면을 이동하고 데이터를 안전하게 넘깁니다.
> - 페이징 (Paging 3): 무한 스크롤의 필수품. 수만 개의 데이터를 쪼개서 가져와 폰이 터지는 걸 막아줍니다.
> - WorkManager: 앱이 죽든 폰이 재부팅되든, 내가 시킨 일(로그 업로드 등)은 기어코 해내는 집요한 백그라운드 일꾼입니다.

## 1. ViewModel & StateFlow / LiveData (상태 관리 방송국)
  > Compose 를 사용하면서 LiveData는 밀려나고, 코틀린 코루틴과 완벽하게 호환되는 **StateFlow**를 사용합니다.

  | **도구** | **역할과 특징** | **실무 트렌드** |
  | --- | --- | --- |
  | **ViewModel** | 화면(UI)이 죽었다 깨어나도 데이터를 안전하게 보관하는 금고. | 필수 중의 필수. 안 쓰는 프로젝트는 없습니다. |
  | **LiveData** | 데이터가 바뀌면 UI에 자동으로 알려주는 생명주기 맞춤형 방송국. | 과거의 영광. 여전히 쓰이지만 점점 밀려나는 추세입니다. |
  | **StateFlow** | 코틀린 코루틴(Coroutine) 기반의 강력한 상태 관리자. | **현재 실무 표준.** 비동기 처리와 데이터 변형 연산자(map, filter 등)가 압도적으로 좋습니다. |
  
💡 실무 팁
- Compose에서 StateFlow 수집(Collect) 시 주의사항
  - 그냥 `collectAsState()`를 쓰면 앱이 백그라운드로 내려가도 계속 데이터를 수집하며 배터리를 파먹습니다.
  - 무조건 <b>`collectAsStateWithLifecycle()`</b>을 사용하세요! (안드로이드 생명주기에 맞춰 알아서 멈추고 다시 시작해 줍니다.)
- 단발성 이벤트(스낵바, 화면 이동) 처리의 최신 트렌드
  - 화면 회전 시 뷰모델에 있던 이벤트가 다시 실행되는 버그를 막기 위해 과거엔 SharedFlow나 Channel을 사용했지만, <br/>
  앱이 백그라운드로 갈 때 이벤트가 허공으로 증발(유실)되는 치명적 버그가 발견되었습니다.
  > - <b>최신 구글 권장</b>: 단발성 이벤트도 상태(State)로 취급! (에러 State 생성 -> UI에서 스낵바 띄움 -> UI가 뷰모델에 알려서 State를 null로 초기화(소비))<br/>
  단, 실무에서는 코드 길이를 줄이기 위해 가벼운 이벤트는 Channel을, 결제 완료 같은 중요 이벤트는 State를 쓰는 식으로 유연하게 타협하기도 합니다.

---

## 2. Navigation Compose (XML 지도의 무덤)
- 동작 방식
  - 앱 최상단에 NavHost라는 도화지를 깔고, composable("경로")라는 함수들로 화면들을 정의한 뒤, NavController를 이용해 웹사이트 URL 이동하듯 화면을 넘깁니다.

💡 실무 팁
- NavController는 최상단에 두세요 (상태 호이스팅)
  - 가장 깊숙한 자식 화면에서 다른 화면으로 이동해야 한다고 그곳까지 NavController 객체를 통째로 넘겨주면 안 됩니다!
  - 코드가 강하게 결합되어 미리보기(Preview)도 깨지고 테스트도 힘듭니다.
  - 자식은 "나 이동할래!"라는 람다 함수(이벤트)만 부모에게 던지고, 이동은 최상단 컴포넌트가 처리하게 만드세요.
- Type-Safe Navigation (최신 트렌드)
  - 과거처럼 오타가 나면 런타임에 앱이 터지는 문자열 경로("detail/{id}") 대신, <br/>
    Kotlin 직렬화(Serializable)를 활용해 Data Class 객체 자체를 넘겨 컴파일 단계에서 에러를 100% 차단하는 가장 안전하고 모던한 방식입니다.

---

## 3. Paging 3 (대용량 데이터와 LazyColumn의 만남)
서버에서 10만 개의 리스트 데이터를 받아올 때 한 번에 가져오면 메모리가 터집니다. <br/>
Paging 3는 사용자가 스크롤을 바닥에 내릴 때쯤 다음 데이터를 미리 쪼개서(Chunking) 부드럽게 가져옵니다.
- Compose와의 찰떡궁합
  - 기존 RecyclerView 시대에는 PagingDataAdapter라는 걸 만들고 세팅하느라 코드가 엄청 길었습니다.
  - Compose에서는 ViewModel에서 페이징 데이터를 collectAsLazyPagingItems()로 받아서, LazyColumn 안에 툭 던져주면 끝납니다.

💡 실무 팁
- 로딩/에러 상태 UI 처리
  - 페이징 데이터를 받아올 때, 어떤 상태(LoadState)인지 아주 쉽게 알 수 있습니다.
  - 이 상태값들을 이용해 화면 맨 밑에 둥글게 돌아가는 로딩 바(CircularProgressIndicator)를 띄워주세요.

---

## 4. DataStore (SharedPreferences의 완벽한 대체자)
(XML 시대의 DataBinding은 Compose 자체가 그 역할을 하므로, 현대 안드로이드 데이터 레이어에서는 DataStore를 사용)
> 왜 써야 할까?
>
> SharedPreferences는 메인 스레드(UI 그리는 곳)를 멈칫하게 만들 위험이 있었습니다. <br/>
> DataStore는 Kotlin 코루틴과 Flow를 기반으로 100% 비동기로 안전하게 동작합니다.

💡 실무 팁
- Preferences DataStore vs Proto DataStore
  - 간단한 key-value(예: isDarkTheme = true)는 Preferences DataStore를 쓰면 충분합니다.
  - 만약 복잡한 커스텀 객체(User Data)를 통째로 저장하고 싶다면 Type-Safety를 보장하는 Proto DataStore를 쓰세요.
- ViewModel과 사용
  - DataStore에서 꺼낸 Flow 데이터를 ViewModel에서 StateFlow로 변환하여 Compose UI에 쏴주는 흐름이 가장 완벽한 로컬 데이터 아키텍처입니다.

---

## 5. WorkManager (UI와 무관한 집요한 백그라운드 일꾼)
앱이 죽든 폰이 재부팅되든, <b>'반드시 실행되어야 하는 보장형 작업'</b>에 사용합니다.
- 주요 기능: 제약 조건을 걸 수 있습니다. "배터리가 충전 중이고, Wi-Fi에 연결되어 있을 때만 이 대용량 사진 100장을 서버에 업로드해!"
- 실무 사용처: 채팅 앱의 메시지 동기화, 로컬 DB 백업, 에러 로그 서버 전송 등.

💡 실무 팁
- 정확한 시간에 울리는 알람용이 아닙니다
  - "내일 아침 7시에 모닝콜 울리게 해줘"라는 기능에 WorkManager를 쓰면 안 됩니다.
  - 안드로이드 시스템이 배터리 절약을 위해 임의로 실행 시간을 늦출 수 있기 때문입니다.
  - 정확한 시간 보장이 필요하다면 AlarmManager를 쓰세요.
