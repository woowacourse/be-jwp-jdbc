package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.util.ArrayList;
import java.util.List;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        List<Object> paramList = new ArrayList<>();
        paramList.add(userHistory.getUserId());
        paramList.add(userHistory.getAccount());
        paramList.add(userHistory.getPassword());
        paramList.add(userHistory.getEmail());
        paramList.add(userHistory.getCreatedAt());
        paramList.add(userHistory.getCreateBy());

        jdbcTemplate.executeQuery(sql, paramList);
    }
}
