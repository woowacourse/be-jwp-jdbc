package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.querybuilder.QueryBuilder;
import com.interface21.jdbc.querybuilder.query.Query;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(UserHistory userHistory) {
        Query query = new QueryBuilder()
                .insert("user_id", "account", "password", "email", "created_at", "created_by")
                .into("user_history")
                .build();

        jdbcTemplate.queryForUpdate(
                query.getSql(),
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    public void log2(Connection connection, UserHistory userHistory) {
        Query query = new QueryBuilder()
                .insert("user_id", "account", "password", "email", "created_at", "created_by")
                .into("user_history")
                .build();

        jdbcTemplate.queryForUpdate2(
                connection,
                query.getSql(),
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
