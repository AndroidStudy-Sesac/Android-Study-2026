> ### 핵심 요약
> - 아키텍처의 목적: 코드가 엉키지 않게 역할을 분리하여, <b>"고치기 쉽고 테스트하기 좋은 앱"</b>을 만드는 것입니다.
> - MVVM (현대 표준): UI(화면)는 데이터를 그리기만 하고, 데이터의 상태 관리는 ViewModel이 전담합니다.
> - MVI (최신 트렌드): 상태(State)를 한 방향으로만 흐르게 만들어, 에러가 어디서 터졌는지 추적하기 쉽게 만듭니다.
> - 클린 아키텍처 (Clean Architecture): 앱을 양파 껍질처럼 나누어, 가장 핵심인 비즈니스 로직(Domain)이 외부(UI, DB)의 변화에 타격받지 않도록 보호하는 설계 철학입니다.

## 1. 아키텍처 패턴의 진화 (MVC부터 MVI까지)
  > 🚨 화면(View)과 로직(Model)을 분리하지 않으면, 나중에 버튼 하나 색깔을 바꾸려다 앱 전체가 터지는 대참사가 발생합니다.
  
  | **패턴** | **구조 및 특징** | **실무 평가 및 장단점** |
  | --- | --- | --- |
  | **MVC**<br/>(Model-View-Controller) | `Activity`가 View와 Controller 역할을 모두 담당. | **(더 이상 안 씀)** 코드가 비대해져서 'God Activity(만능 액티비티)'가 탄생함. 유지보수 지옥. |
  | **MVP**<br/>(Model-View-Presenter) | `Presenter`가 View와 Model 사이의 중재자 역할을 함. View와 Presenter가 1:1로 강하게 결합됨. | **(과도기)** MVC보단 낫지만, 화면이 복잡해질수록 Presenter 코드도 무한정 길어지는 단점이 있음. |
  | **MVVM**<br/>(Model-View-ViewModel) | **[현재 안드로이드 표준]** `ViewModel`이 상태를 들고 있고, `View`는 그 상태를 관찰(Observe)하여 스스로 화면을 업데이트함. | View와 ViewModel이 분리되어 코드가 깔끔해짐. 단, 상태(State) 관리가 복잡해지면 꼬일 수 있음. |
  | **MVI**<br/>(Model-View-Intent) | **[최신 트렌드]** 사용자의 행동을 `Intent`로 정의하고, 상태(`State`)를 **단방향**으로만 흘려보냄. | 무조건 한 방향으로만 데이터가 흐르므로 디버깅이 예술적으로 쉬움. 단, 템플릿(보일러플레이트) 코드를 많이 짜야 해서 귀찮음. |

💡 실무 팁
- "View는 바보다"라고 생각
  - UI(Activity/Fragment/Compose)에는 조건문(if/else)이나 복잡한 계산 로직이 없어야 합니다.
  - 오직 ViewModel이 던져주는 데이터를 "어떻게 예쁘게 그릴까?"만 고민해야 합니다.
- MVI 도입, 무조건 좋을까?
  - MVI가 핫하다고 해서 단순한 로그인 화면에까지 도입할 필요는 없습니다. 프로젝트의 규모와 팀원의 숙련도에 맞춰 MVVM과 적절히 혼용하는 것이 베스트입니다.

---

## 2. ViewModel (상태 관리의 심장)
아키텍처에서 가장 중요한 중간 관리자입니다. <br/>
화면이 회전해서 파괴되어도, ViewModel은 꿋꿋이 살아남아 UI에 보여줄 '데이터(상태)'를 쥐고 있습니다.

💡 실무 팁
- 안드로이드 클래스를 절대 넣지 마세요
  - ViewModel에 Context, Activity, View 객체를 전달하는 순간 메모리 릭(Memory Leak) 확정입니다. <br/>(화면이 죽어도 ViewModel이 UI 객체를 꽉 쥐고 놔주지 않기 때문입니다.)
- LiveData vs StateFlow
  - 예전에는 LiveData를 많이 썼지만, 요즘은 Kotlin 코루틴과 찰떡궁합인 **StateFlow**를 쓰는 것이 실무 표준이 되었습니다. <br/>비동기 처리와 상태 업데이트가 훨씬 강력합니다.

---

## 3. 클린 아키텍처 (Clean Architecture)
  > 🚨 앱의 규모가 커질수록 "코드를 어디에 둬야 하지?"라는 고민이 생깁니다.<br/>
  > 클린 아키텍처는 코드를 3개의 계층(Layer)으로 나누어 이 고민을 해결해 줍니다.

  | **계층 (Layer)** | **역할** | **포함되는 컴포넌트** |
  | --- | --- | --- |
  | **Presentation (UI)** | 화면을 그리고, 사용자의 터치 입력을 받습니다. | Activity, Fragment, Compose, **ViewModel** |
  | **Domain (비즈니스 로직)**| **앱의 핵심 규칙과 로직이 모여있는 곳입니다.<br/>** 다른 계층에 대해 아무것도 모릅니다. | **UseCase**, Entity(모델), Repository Interface |
  | **Data (데이터)**| 데이터를 어디서(서버 vs 로컬DB) 가져올지 결정하고 제공합니다. | **Repository** 구현체, Retrofit, Room DB |

  > 의존성 규칙 (Dependency Rule): 화살표의 방향은 무조건 바깥쪽에서 안쪽(Domain)으로만 향해야 합니다.
  > - Presentation ➡️ Domain ⬅️ Data

💡 실무 팁
- Domain 계층에는 안드로이드 딱지를 떼세요
  - Domain 계층(UseCase) 파일에는 import android... 코드가 단 한 줄도 들어가면 안 됩니다.
  - 오직 순수한 Kotlin(또는 Java) 코드로만 작성되어야, 나중에 UI나 DB가 바뀌어도 핵심 로직은 전혀 수정할 필요가 없습니다.
- UseCase는 '행동' 하나만 담당합니다
  - GetUserProfileUseCase, LoginUseCase처럼 이름만 봐도 무슨 일을 하는지 알 수 있게 쪼개세요.
  - ViewModel이 뚱뚱해지는 걸 완벽하게 막아줍니다.
- Repository (데이터 창구)
  - ViewModel은 "이 데이터가 서버에서 온 건지, 로컬 DB에서 온 건지" 몰라야 합니다.
  - 그 판단과 데이터 조달은 Data 계층의 Repository가 뒤에서 다 알아서 해줘야 합니다.
    
