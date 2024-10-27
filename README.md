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

## 기능 구현 목록

### 1단계

- 사용자 데이터를 메모리가 아닌 DB에 저장할 수 있도록 개선
  - [x] InMemoryUserRepository 사용 로직 UserDao 사용으로 전환
  - [x] UserDao 테스트 성공

- 반복적인 DB 관련 작업 코드 개선
  - [x] JdbcTemplate 클래스에 구현

## 2단계

- JDBC라이브러리와 개발자의 역할 분리 및 리펙터링
- JDBC 라이브러리
  - [x] connection 생성 리펙터링
  - [x] statement 준비 및 실행
    - 가변 인자
  - [x] resultSet 생성
    - RowMapper 구현 (함수형 인터페이스, 제네일)
  - [x] 예외 처리
    - checked exception 처리
  - [x] Connection, Statement, ResultSet 객체 사용 및 close 로직 리펙터링 (재사용되도록)
    - 익명 클래스 사용
    - try-with-resources

## 3단계

- [x] 비밀번호 변경 transaction 처리
  - [x] jdbc: insert or update 문 connection 주입 받도록 수정
  - [x] dao: insert or update 문 connection 주입 받도록 수정

## 4단계

- [x] Transaction synchronization 적용 : DAO가 Connection 객체를 파라미터로 전달받아 사용하지 않도록
  - [x] DataSourceUtils 구현
  - [x] TransactionSynchronizationManager 구현

- [x] 트랜잭션 서비스 추상화 : 인터페이스를 활용하여 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직 분리
  - [x] 인터페이스 구현 
  - 구현 클래스 구현
    - [x] 비즈니스 로직
    - [x] 데이터 액세스 로직

