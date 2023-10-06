package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.TransactionManager;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final TransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final TransactionManager transactionManager, final JdbcTemplate jdbcTemplate) {
        this.transactionManager = transactionManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        log.info("query: {}", sql);
        transactionManager.save(
                (connection, entity) -> jdbcTemplate.executeUpdate(
                        connection,
                        sql,
                        userHistory.getUserId(),
                        userHistory.getAccount(),
                        userHistory.getPassword(),
                        userHistory.getEmail(),
                        userHistory.getCreatedAt(),
                        userHistory.getCreateBy()
                ), userHistory);
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        log.info("query: {}", sql);
        jdbcTemplate.executeUpdate(
                connection,
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
