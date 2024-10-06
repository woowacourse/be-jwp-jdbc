# 만들면서 배우는 스프링

## JDBC 라이브러리 구현하기 기능 요구사항

### 1단계 - JDBC 라이브러리 구현하기

- [ ] UserDao 의 미구현된 메서드들 완성
- [ ] JdbcTemplate 구현해 UserDao 의 중복되는 코드 제거
    - [ ] SQL 쿼리를 JdbcTemplate 이 갖고 있게 수정
    - [ ] SQL 쿼리에 인자 넣는 작업을 JdbcTemplate이 하도록 수정
    - [ ] SQL 쿼리를 실행하는 작업을 JdbcTemplate이 하도록 수정
    - [ ] SQL 쿼리 실행 결과를 객체로 매핑하는 작업을 JdbcTemplate이 하도록 수정
- [ ] JdbcTemplate 이용해 UserHistoryDao 의 중복되는 코드 제거

## JDBC 라이브러리 구현하기 가이드

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
