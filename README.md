# 만들면서 배우는 스프링

## JDBC 라이브러리 구현하기

### 학습목표

- JDBC 라이브러리를 구현하는 경험을 함으로써 중복을 제거하는 연습을 한다.
- Transaction 적용을 위해 알아야할 개념을 이해한다.

### 시작 가이드

1. 이전 미션에서 진행한 코드를 사용하고 싶다면, 마이그레이션 작업을 진행합니다.
    - 학습 테스트는 강의 시간에 풀어봅시다.
2. LMS의 1단계 미션부터 진행합니다.

## 준비 사항

- 강의 시작 전에 docker를 설치해주세요.

## 학습 테스트

1. [ConnectionPool](study/src/test/java/connectionpool)
2. [Transaction](study/src/test/java/transaction)

### STEP1 - 기능 요구 사항

- [x] 개발자 편의를 위해 JDBC 라이브러리 구현

**Feat**

- [x]  UserDao - findAll 구현
- [x]  UserDao - update 구현
- [x]  UserDao - findByAccount 구현

**Refactor**

- [ ]  InMemoryDB → DAO로 교체
- [x]  클라이언트 중복 코드 JdbcTemplate 프레임 워크로 추상화
- [x]  JdbcTemplate 이용하도록 개선

### STEP2 - 기능 요구 사항

**Refactor**

- [x] 중복된 Jdbc 로직 템플릿 콜백 메서드 패턴으로 리팩터링

### STEP3 - 기능 요구 사항

**Feat**

- [x] 유저의 비밀번호 변경할 수 있는 기능 추가
- [x] 비밀번호 변경 시 누가 언제 어떤 비밀번호로 바꿨는지 이력을 남긴다.
- [x] 비밀번호 변경 기능이 원자성을 가지도록 트랜잭션 적용

### STEP4 - 기능 요구 사항

- [x] Transaction synchronization 적용하기
    - [x] connection 객체를 가져오는 부분 DataSourceUtils를 사용하도록 수정
    - [x] TransactionSynchronizationManager가 올바르게 작동하도록 수정
- [x] 트랜잭션 서비스 추상화하기
    - [x] 유저 service에 남아있는 데이터 액세스 로직 제거


