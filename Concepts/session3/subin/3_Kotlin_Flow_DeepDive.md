# Flow 심화

## backpressure 느낌 · buffer/conflate/collectLatest · shareIn/stateIn · 예외/취소 · hot/cold 설계

## Flow 심화 핵심 요약

- Flow는 기본적으로 **Cold**: collect할 때 실행
- 값이 너무 빨리 들어오면 처리 전략이 필요 → **buffer / conflate / collectLatest**
- 여러 곳에서 collect하면 중복 실행될 수 있음 → **shareIn / stateIn**으로 공유
- 취소/예외는 스트림 품질을 좌우 → **catch / onCompletion / retry**(키워드) + lifecycle 수집

---

## 1) Backpressure를 Flow에서 어떻게 느끼나?

Backpressure란 “생산 속도 > 소비 속도”일 때 발생하는 압력.

Flow에서는 다음 현상으로 체감됨:

- UI 렌더가 느린데 값이 계속 방출됨
- 검색어/스크롤 이벤트가 폭발적으로 발생
- 네트워크 응답이 쌓이면서 처리 지연

> Rx처럼 타입(Flowable)로 강제하기보다 Flow는 연산자로 전략을 선택하는 느낌
> 

---

## 2) 처리 전략 3종 세트: buffer / conflate / collectLatest

### 2-1) buffer

- 생산과 소비 사이에 버퍼를 두고 **잠시 쌓아둠**
- 소비가 따라잡을 때까지 큐잉하는 전략

**감각**: 일단 다 받되, 잠깐 쌓아두자

---

### 2-2) conflate

- 중간 값은 버리고 **최신 값만 유지**
- UI 상태처럼 “최신이 중요”할 때 유리

**감각**: 중간 프레임은 버리고 최신만 보여줘

---

### 2-3) collectLatest

- 새 값이 오면 이전 처리 블록을 **취소하고 최신만 처리**
- 검색/자동완성/이미지 로딩처럼 “최신 요청만 의미 있음”에서 강력

**감각**: 이전 작업은 중단, 최신만 처리

---

## 3) flowOn / withContext 다시 정리(심화 관점)

### flowOn

- **업스트림**(emit/변환) 컨텍스트를 바꿈
- 예: DB/네트워크/파싱 같은 upstream 부담을 IO/Default로

### withContext

- 코드 블록 자체를 바꾸는 전환
- Flow 내부/외부 어디서든 사용 가능하지만, Flow에서는 보통 flowOn으로 upstream을 정리하는 편이 깔끔함

---

## 4) Cold → Shared: shareIn / stateIn (중복 실행 방지 핵심)

### 문제

- Cold Flow는 collect할 때마다 실행될 수 있음
    
    → 동일 네트워크 호출이 여러 번 발생하는 구조가 생김
    

### 해결: 공유로 변환

- `shareIn`: Flow를 **SharedFlow**로 공유
- `stateIn`: Flow를 **StateFlow**로 공유(최신 값 보유)

**감각**

- shareIn = 이벤트/브로드캐스트 공유
- stateIn = 상태로 고정(최신 값 유지)

---

## 5) StateFlow/SharedFlow 심화 설계 포인트

### StateFlow(상태)

- 항상 “현재 값”이 존재
- UI 렌더링과 결합이 자연스러움
- `UiState(data class)` + `copy()` 패턴과 궁합이 좋음

### SharedFlow(이벤트)

- 일회성 이벤트는 정책이 중요
    - replay(재전달 여부)
    - buffer(유실 방지)
- 이벤트를 무조건 “저장”하면 중복 발생, 무조건 “저장 안 함”이면 유실 가능
    
    → 팀 정책/UX에 따라 설계
    

---

## 6) 예외/취소 (Flow 품질의 핵심)

### catch

- 업스트림 예외를 스트림 내부에서 처리
- UI는 “에러 상태”로 변환해 흘리는 패턴이 안정적

### 취소

- Flow 수집은 lifecycle에 묶어야 안전
- 수집 중단=코루틴 취소로 이어질 수 있음 → 최신 값 처리/정리(onCompletion) 관점 중요

---

## 7) 패턴 3개(심화 연결)

### 패턴 A) 검색어 입력

- `debounce + distinctUntilChanged + flatMapLatest`
- 최신 입력만 요청 유지, 타이핑 폭주 방지

### 패턴 B) UI 상태는 StateFlow, 이벤트는 SharedFlow

- 상태/이벤트를 섞으면 중복/유실 문제가 커짐

### 패턴 C) 무거운 변환은 upstream에서 처리

- `map`에서 큰 파싱/정렬이 있으면 `flowOn(Default)`로 옮겨 UI 프리징 방지
