package com.techcourse.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy());
    }

    public void log(final Connection connection, final UserHistory userHistory) throws SQLException {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        try (final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, userHistory.getUserId());
            preparedStatement.setString(2, userHistory.getAccount());
            preparedStatement.setString(3, userHistory.getPassword());
            preparedStatement.setString(4, userHistory.getEmail());
            preparedStatement.setObject(5, userHistory.getCreatedAt());
            preparedStatement.setString(6, userHistory.getCreateBy());
            preparedStatement.executeUpdate();
        }
    }
}
