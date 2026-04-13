# Coroutine 심화

## 취소(Cancellation) · 예외(Exception) · 구조적 동시성(Structured Concurrency) · Supervisor

## 전체 핵심 요약

- 코루틴의 “실무 품질”은 **취소/예외/부모-자식 관계**를 제대로 이해하는지로 갈림
- 코루틴은 scope 안에서 실행되고, **scope가 사라지면 같이 취소**되는 구조가 기본(구조적 동시성)
- 예외는 기본적으로 **부모로 전파**되어 형제까지 취소시킬 수 있음 → 필요하면 **supervisor**로 격리
- 취소는 강제 종료가 아니라 **협력적(cooperative)** 으로 이루어짐 → 중단 지점(suspend) 또는 취소 체크가 중요

---

# 1) Cancellation(취소) — 코루틴의 기본 동작

## 1-1) 취소는 “협력적”이다

- 코루틴은 `job.cancel()`로 취소 요청을 받지만,
- 실제로 멈추는 건:
    - `delay`, `withContext`, `yield` 같은 **suspend 지점**
    - 또는 `ensureActive()` 같은 **취소 체크**에서 일어난다.

### 왜 협력적이어야 하나?

- 스레드처럼 강제 kill은 리소스 정리/일관성을 깨뜨리기 쉬움
- 코루틴은 안전한 종료를 위해 협력적 모델을 선택

---

## 1-2) 취소와 예외: CancellationException

- 취소는 내부적으로 `CancellationException`으로 표현되는 경우가 많음
- 일반 예외처럼 무작정 잡아먹으면 **취소가 무시**될 수 있어 주의

> 실무 감각: catch에서 `CancellationException`은 보통 다시 throw하거나 별도 처리
> 

---

## 1-3) finally는 중요하다

- 취소가 발생해도 `finally`는 실행되므로 리소스 정리에 적합

---

# 2) Exception(예외) — launch vs async의 차이

## 2-1) launch의 예외

- `launch`는 결과를 돌려주지 않음(Job)
- 내부에서 발생한 예외는 “잡지 않으면” 부모 scope로 전파될 수 있음

## 2-2) async의 예외

- `async`는 결과를 `Deferred`로 돌려줌
- 예외는 주로 `await()` 시점에 던져짐(지연 전파)
- await를 안 하면 예외가 늦게 드러나거나 놓칠 수 있음

---

## 2-3) CoroutineExceptionHandler의 위치 감각

- `CoroutineExceptionHandler`는 “잡히지 않은 예외(uncaught)” 처리에 유효
- try-catch로 잡힌 예외에는 관여하지 않음

> 실무 감각: 전역 핸들러보다 “요청 단위 try-catch”가 예측 가능성이 높음
> 

---

# 3) 구조적 동시성(Structured Concurrency) — 부모/자식 관계

## 3-1) 기본 규칙

- 코루틴은 scope 안에서 시작한다
- 부모 코루틴이 끝나면 자식도 같이 끝난다(취소 전파)
- “어디에 매달린 작업인지”가 코드 구조로 드러난다

### 왜 중요한가?

- GlobalScope 같은 방식은 작업이 떠돌며 누수/좀비 작업 발생
- lifecycle 끝난 뒤 UI 업데이트 같은 크래시의 원인이 됨

---

## 3-2) coroutineScope

- “이 블록 안에서 시작한 자식들이 끝날 때까지 기다리는” 범위
- 자식 중 하나가 실패하면 기본적으로 전체가 실패(전파)

**감각**: “다 같이 성공/실패 운명 공유”

---

# 4) Supervisor — 실패 격리(부분 성공)

## 4-1) supervisorScope / SupervisorJob

- 자식 코루틴 하나가 실패해도 **다른 자식에게 실패를 전파하지 않게** 함
- “부분 성공” 정책 구현에 유리

**감각**: “너 실패해도 나는 계속”

---

# 5) Android 실무에서의 적용 감각

## 5-1) lifecycle에 묶기

- `viewModelScope` / `lifecycleScope`로 시작하면
- 화면/VM 수명과 함께 취소되어 안전

## 5-2) 병렬 호출 정책

- “둘 다 성공해야 한다” → coroutineScope + async/await
- “하나 실패해도 나머지 보여준다” → supervisorScope + 개별 처리

---

# 6) 자주 하는 실수

- 취소를 `Exception`으로 뭉뚱그려 catch해서 취소를 삼켜버림
- async를 남발해서 예외/취소 흐름이 복잡해짐
- 구조적 동시성을 깨는 전역 scope 사용
- supervisor가 필요한 상황에서 기본 scope로 묶어서 “하나 실패에 전체 취소” 발생
