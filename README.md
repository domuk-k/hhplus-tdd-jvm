## 프로젝트 소개

이 프로젝트는 포인트 서비스 시스템을 구현하여, 사용자의 포인트 충전, 사용, 내역 조회 등의 기능을 제공하는 서비스입니다.
핵심 목표는 테스트 주도 개발(TDD) 을 통해 기능을 안정적으로 구현하고, 테스트 가능한 코드를 작성하는 것이었습니다.

## 프로젝트 구조

```sh
├── ApiControllerAdvice.kt
├── TddApplication.kt # 애플리케이션 진입점
├── common
│   ├── Response.kt # 여러 Controller가 넘겨 받을 Response<T> 타입 정의
│   └── util
│       ├── AsyncConcurrencyHandler.kt # 코루틴기반 비동기 동시성 처리 시도
│       ├── ConcurrencyHandler.kt # 인터페이스: 동시성 처리 방법 정의
│       ├── ReentrantLockConcurrencyHandler.kt # 구현 : 재진입 락 동시성 처리
│       ├── SerializedConcurrencyHandler.kt # 구현 : 직렬화 동시성 처리
│       └── SynchronizedConcurrencyHandler.kt # 구현 :동기화 동시성 처리
├── database # 학습용 메모리 기반 임시 DB
│   ├── PointHistoryTable.kt
│   └── UserPointTable.kt
└── point # 포인트 서비스 도메인
    ├── domain
    │   ├── PointService.kt # 포인트 서비스
    │   ├── command # 포인트 Service가 넘겨 받을 Command(Mutation용도) 객체
    │   ├── entity # 포인트 Repository에서 사용할 엔티티
    │   ├── policy # 포인트 Service가 사용할 정책 객체
    │   ├── repository # 포인트 Repository
    │   └── request # 포인트 Controller에서 넘겨 받을 PointRequest 객체
    └── interfaces # 포인트 컨트롤러
        └── PointController.kt
```

## 동시성 제어 방법

### 문제 정의 : 명확히 말해보기

- 같은 유저의 포인트 잔량을 수정하는 요청이 일정 시점에 몰리면 기대한 대로 동작하지 않는다.
- 같은 포인트 잔량 객체 (또는 row)를 여러 쓰레드에서 동시에 "읽고" "덮어쓰려고" 하면 문제가 발생한다.
- 공유 자원에 대해 여러 쓰레드가 동시에 접근하여 "(사실은 동기화되어야하는, 쪼개져서는 안되는) 일련의 (수정)작업을 하면",
  해당하는 일련의 트랜젝션들이 엉키면서 기대와 다른 결과가 나온다.
  > 자원에 대한 어떤 작업들은 한 덩어리(원자)로서, 동기화되어야한다.

### 채택한 방법

- 사용자 단위 ReentrantLock 기반의 애플리케이션 레벨 비관적 락

ReentrantLock은 "어떤 쓰레드가 어떤 유저에 대한 포인트 잔량 수정 작업 중"에는 배타적인 임계구간에서 작업을 진행하게 해준다. 그동안 같은 유저에 대한 포인트 잔량 수정 작업이 들어오면 대기하게 해준다.

현재 과제에서는 다음과 같은 제약 조건이 존재한다:
• JPA 사용 불가 (@Version 같은 낙관적 락 적용 불가)
• DB 엔티티 혹은 TableClass 수정 불가 (버전 필드 추가 불가)
• 코틀린 기반 프로젝트
• 동시성 이슈가 발생할 수 있는 포인트 충전/사용 로직이 존재

이러한 제약 조건 하에서는, 일반적인 낙관적 락 방식이나 DB 수준의 락(FOR UPDATE)을 활용하기 어렵기 때문에, 애플리케이션 레벨에서 동시성을 제어하는 방식이 필요하다.

#### 쪼개보기

> "ConcurrentHashMap을 사용하여 유저ID 별로 ReentrantLock을"

- 결국 Map이라는 구조로 ReentrantLock이라는 자원을 유저ID별로 관리한다.
- 쓰레드들은 이 Map에 접근해서 자신이 원하는 유저ID에 대한 ReentrantLock을 얻어올거다.
- 근데 이 Map에도 여러 쓰레드가 접근하게 된다. 그래서 이 Map 자체에 대한 동시성 제어도 사실 필요하다?
- 동시성이 고려된 Map이 필요하다?
- ConcurrentHashMap은 Map구현체들 중에서도 동시성이 고려된 구현체다.
  - CAS(compare and swap)연산과 트리구조(red-black tree) 덕분에 안정적으로 동시성을 제어한다고 한다.
  - "분할 락", 맵 전체가 아닌 일부분에 락을 사용. 여러 쓰레드가 동시에 접근해도 빠르다.
  - 읽기는 대부분 락을 사용하지 않는다.

> 어떤 유저에 대한 포인트 잔량 수정 작업을 할 때

- 쓰레드는 맵에서 일단 key에 대응하는 Lock을 얻어오게 된다. ConcurrentHashMap.computeIfAbsent 호출해서 lock(sync)를 얻어온다
  - computeIfAbsent의 스펙 요약
    - 해당 key에 대응하는 value가 있는 지 compute해서 반환.
    - 해당 key에 value가 null이면 블럭 안의 코드를 "원자적으로" 실행하고 결과를 map에 등록하고, 반환한다.
    - 블럭 깔끔하게 써라. map을 변형시키지 말고.

> 쓰레드가 끝날 때까지 다른 쓰레드가 그것에 접근하지 못하도록

- 이렇게 얻어온 ReentrantLock을 통해 임계구간을 만들어서(setExclusiveOwnerThread) 임계구간 내에서 자신이 원하는 유저의 포인트 잔량 수정 작업을 한다.

#### 검토한 다른 방법들

은 많은 데 잠시만요;
