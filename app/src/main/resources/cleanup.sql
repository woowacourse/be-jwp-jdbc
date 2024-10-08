SET REFERENTIAL_INTEGRITY FALSE; -- 외래키 제약 조건 해제

DELETE FROM users;
ALTER TABLE users ALTER COLUMN id RESTART;

DELETE FROM user_history;
ALTER TABLE user_history ALTER COLUMN id RESTART;

SET REFERENTIAL_INTEGRITY TRUE; -- 외래키 제약 조건 재설정