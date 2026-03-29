> ### 핵심 요약
> - Recomposition (재구성): 화면 전체를 다시 그리는 게 아니라, 상태가 바뀐 '그 부분'만 똑똑하게 다시 그립니다.
> - State Hoisting: 자식 컴포넌트는 바보로 만들고, 똑똑한 부모가 상태를 관리하게 위로 끌어올리는 필수 패턴입니다.
> - Modifier: UI 요소의 크기, 색상, 이벤트를 조립하는 레고 블록입니다. (순서가 생명!)

## 1. State (상태)와 Recomposition (재구성)
과거 XML 시대에는 텍스트를 바꾸려면 textView.text = "안녕" 이라고 직접 명령(명령형 UI)을 내려야 했습니다.<br/>
하지만 Compose는 선언형 UI입니다.<br/>
상태(데이터)를 던져주면 알아서 그려줍니다.

  | **개념** | **역할과 특징** |
  | --- | --- |
  | **`MutableState`** | 값이 변하면, 이 값을 보고 있는 Compose UI에게 "야! 나 변했어 다시 그려!"라고 찌르는 트리거입니다. |
  | **`remember`** | 화면이 다시 그려질 때(Recomposition) 상태값이 날아가지 않도록, Compose 메모장에 잠깐 적어두는 함수입니다. |
  | **`rememberSaveable`** | 화면이 가로/세로로 회전해서 Activity가 파괴되었다가 다시 생겨도, 꿋꿋하게 값을 기억해 내는 강력한 메모장입니다. |
  | **`Recomposition`** | 상태값이 변했을 때 화면을 다시 그리는 과정. **전체를 다시 그리지 않고, 바뀐 데이터와 연결된 딱 그 부분만 갱신**합니다. |

💡 실무 팁
- Compose 함수 안에 무거운 연산을 그냥 두지 마세요
  - Recomposition은 1초에도 수십 번씩 일어날 수 있습니다.
  - 리스트를 정렬하거나 복잡한 계산을 하는 코드를 그냥 두면 폰이 멈춥니다.
  - 반드시 remember 로 감싸서 한 번만 계산하게 하거나, 가장 좋은 건 ViewModel에서 다 계산한 뒤 결괏값만 UI로 넘겨주는 것입니다.

---

## 2. State Hoisting (상태 끌어올리기)
- 동작 방식
  - 내부 상태를 상위 컴포넌트로 위임하여, 하위 컴포넌트는 주입받은 데이터로 화면을 그립니다.
  - 사용자 이벤트는 콜백(Lambda)으로 상위에 전달만 하는 무상태(Stateless) 구조를 만드는 패턴입니다.

💡 실무 팁
- 미리보기(@Preview) 렌더링 실패의 주원인
  - UI 컴포넌트 내부에 ViewModel을 직접 주입하면 안드로이드 스튜디오의 Preview 기능이 동작하지 않습니다.
  - 재사용성과 테스트 용이성을 위해 컴포넌트는 오직 '순수 데이터(String, Int 등)'와 '이벤트 함수'만 매개변수로 받도록 철저히 분리해야 합니다.

---

## 3. Side-effects (부수 효과)
Compose의 목적은 오직 '화면 그리기'입니다.<br/>
화면 그리는 과정 중간에 네트워크 통신을 하거나 데이터베이스를 건드리면 앱이 터집니다.<br/>
이렇게 UI 렌더링 외의 작업을 Side-effect라고 부르며, 이를 안전하게 처리하는 '전용 API(격리 구역)'이 필요합니다.

  | **도구** | **실무 사용처** |
  | --- | --- |
  | **`LaunchedEffect`** | 화면이 처음 켜질 때 딱 한 번 API 호출하기, 스낵바 띄우기 (코루틴 스코프 제공) |
  | **`DisposableEffect`** | 화면이 켜질 때 센서를 켜고, 화면이 죽을 때(onDispose) 센서 끄기 (자원 정리용) |
  | **`SideEffect`** | Compose의 상태를 Compose가 아닌 외부 세계(예: Analytics 로깅)로 전달할 때 |

💡 실무 팁
- API 무한 호출 주의
  - Compose 함수 본문에 viewModel.fetchData()를 직접 작성하면, Recomposition이 일어날 때마다 API 요청이 무한 반복되는 심각한 성능 이슈가 발생합니다.
  - 최초 1회 로딩이나 특정 상태가 변할 때만 통신해야 하므로 반드시 LaunchedEffect 블록 내부에서 호출해야 합니다.

---

## 4. Modifier (UI 수식어)
UI 컴포넌트(Text, Button 등)의 크기, 여백, 배경색, 클릭 행동을 꾸며주는 도구입니다.

💡 실무 팁 (가장 많이 하는 실수 🚨)
- Modifier는 체이닝(Chaining) '순서'가 최종 렌더링을 결정합니다
  - 호출되는 순서대로 뷰를 감싸며(Wrapping) 적용되므로, 기존 XML이나 CSS와 달리 작성 순서가 결과물에 절대적인 영향을 미칩니다.
  - Modifier.padding(10.dp).background(Color.Red) -> 바깥쪽 여백을 10dp 확보한 뒤, 안쪽 공간을 빨간색으로 칠함. (빨간 박스 외부에 여백 생성)
  - Modifier.background(Color.Red).padding(10.dp) -> 먼저 전체 영역을 빨간색으로 칠하고, 그 안쪽으로 10dp 여백을 둠. (빨간 박스 내부에 여백 생성)
- 원하는 디자인이 나오지 않는다면 Modifier의 적용 순서를 "위에서 아래로 깎아 나간다"는 느낌으로 재점검해야 합니다.

---

## 5. UI 배치의 기본 도구들
- Layout: Column(세로 배치), Row(가로 배치), Box(겹쳐서 배치 - 기존 FrameLayout 대체).
- List: LazyColumn, LazyRow (기존 RecyclerView를 대체하며, Paging3 라이브러리와 완벽히 연동됨).
- Theme & MaterialDesign: 앱 전체의 색상, 타이포그래피, 형태(Shape)를 중앙에서 통제하는 디자인 시스템.
- Gestures & Animation: 클릭, 스와이프 등의 터치 이벤트 처리와 animateDpAsState 등을 활용한 간결한 상태 기반 애니메이션 구현.

💡 실무 팁
- CompositionLocal (암시적 데이터 전달)
  - 트리의 깊은 하위 계층까지 테마 색상이나 Context를 전달해야 할 때, 모든 컴포넌트의 매개변수로 일일이 넘기면(Prop drilling) 코드가 매우 지저분해집니다.
  - CompositionLocal을 사용하면 명시적인 매개변수 전달 없이 암시적으로 데이터를 하위 트리에 제공할 수 있습니다.
- 단, 남용 금지
  - 데이터의 출처를 명확히 추적하기 어려워지므로, 비즈니스 로직(ViewModel 데이터 등) 전달에는 사용을 지양하고, <br/>
  앱 전역 설정(Theme, Context, Font 등)에만 제한적으로 사용해야 합니다.
