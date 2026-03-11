# 🚀 Modern Android & CS Study

안드로이드 스터디입니다. 각자 공부한 개념을 기록하고 이를 공통 프로젝트에 적용하여 성장합니다.


## 📌 1. 브랜치(Branch) 생성 규칙

작업을 시작하기 전, 항상 `main` 브랜치를 최신화(`git pull`)한 후 작업 성격에 맞는 브랜치를 생성합니다.

* **개념 스터디 정리용:** `session회차/이름`
* 예시: `session1/subin`, `session2/jiyoung`


* **🌟 공통 프로젝트 작업용:** `feat/compose-auth/본인이름_작업내용`
* 예시: `feat/compose-auth/subin_login_ui`, `feat/compose-auth/jiyoung_room_db`


* **개인 프로젝트 작업용:** `feat/프로젝트명/작업내용`
* 예시: `feat/Orange/login-ui`, `feat/Apple/room-db`


---

## 📌 2. 폴더 및 파일 구조 (중요 ⭐️)

이 레포지토리는 개념 문서와 여러 개의 안드로이드 프로젝트가 함께 존재합니다. **Merge 충돌 방지 및 안드로이드 스튜디오 Gradle 에러를 막기 위해 아래 구조를 반드시 지켜주세요!**

### [📂 Concepts: 개념 스터디 폴더]

최상단 루트에 마크다운을 만들면 충돌이 발생합니다. **반드시 회차별 폴더 -> 본인 이름 폴더** 안에 작성하세요.

### [📂 Study_Projects: 공통 실습 프로젝트 폴더]

스터디 공통 과제인 로그인/회원가입 앱이 들어가는 공간입니다. **`ComposeAuth_본인이름`** 으로 각자의 안드로이드 프로젝트를 세팅합니다.


⚠️ **주의:** 안드로이드 스튜디오에서 프로젝트를 열 때는 레포지토리 최상단이 아닌, **반드시 본인이 작업할 개별 앱 폴더(예: `ComposeAuth_Subin` 또는 `Leafy`)를 직접 Open** 해야 정상적으로 빌드됩니다!

```text
📦 Android-CS-Study
 ┣ 📂 Concepts                 
 ┃ ┣ 📂 session1               # 회차별 폴더
 ┃ ┃ ┣ 📂 subin                # 반드시 본인 이름 폴더 생성 후 작성!
 ┃ ┃ ┃ ┗ 📜 1_Lifecycle.md
 ┃ ┃ ┗ 📂 jiyoung
 ┣ 📂 Study_Projects           # 🌟 공통 프로젝트 공간
 ┃ ┣ 📂 ComposeAuth_Subin      # 수빈님의 안드로이드 스튜디오 프로젝트
 ┃ ┗ 📂 ComposeAuth_Jiyoung    # 지영님의 안드로이드 스튜디오 프로젝트
      

```

---

## 📌 3. 커밋 메시지 (Commit Message) 규칙

스터디 저장소의 특성을 살려 직관적이고 깔끔하게 작성합니다. 영어 태그 뒤에 콜론(`:`)을 붙이고 띄어쓰기를 한 번 합니다.

* **형식:** `태그: [분류] 작업 내용`

| 태그 | 설명 | 커밋 메시지 예시 |
| --- | --- | --- |
| **docs** | 마크다운 개념 문서 작성 및 수정 | `docs: [session1] 수빈 MVVM 패턴 개념 정리` |
| **feat** | 새로운 기능 및 UI 추가 | `feat: [ComposeAuth_Subin] 공통 TextField 슬롯 컴포넌트 추가`<br>

<br>`feat: [Leafy] 바텀 네비게이션 추가` |
| **fix** | 문서 오타 및 프로젝트 버그 수정 | `fix: [TypeTest-Kotlin] 결과 화면 UI 렌더링 버그 수정` |
| **refactor** | 기능 변화 없는 코드 구조 개선 | `refactor: [TypeTest-Kotlin] 로그인 버튼 Slot API로 분리` |
| **chore** | 폴더 생성, 파일 이동, gradle 세팅 | `chore: [Setting] 스터디 공통 규칙 및 README 세팅` |

---

## 📌 4. PR (Pull Request) 및 Merge 규칙

1. 깃허브에서 `main` 브랜치를 향해 PR을 생성합니다.
2. **PR 제목은 커밋 메시지와 동일한 형식**으로 적어주세요.
* 예: `[session1] subin 1회차 생명주기 및 Context 정리 완료`
* 예: `[ComposeAuth_Subin] 회원가입 Room DB Insert 로직 구현`


3. 세미나(스터디 모임)에서 각자의 코드와 문서를 리뷰합니다.
4. **최소 1명 이상의 팀원에게 `Approve`를 받은 후**, 각자의 PR을 `main` 브랜치로 **Merge** 합니다. (독단적 Merge 절대 금지!)
