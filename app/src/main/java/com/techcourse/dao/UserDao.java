package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_MAPPER = (rs) -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select * from users";
        return jdbcTemplate.query(sql, USER_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select * from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select * from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_MAPPER, account);
    }
}
