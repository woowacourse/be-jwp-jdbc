package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static Logger log = LoggerFactory.getLogger(UserDao.class);
    private static RowMapper<User> USER_ROW_MAPPER = rs ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Connection connection, User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.info("[LOG] insert user into users");
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(Connection connection, User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        log.info("[LOG] update user");
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll(Connection connection) {
        String sql = "select id, account, password, email from users";
        log.info("[LOG] select all from users");
        return jdbcTemplate.query(connection, sql, USER_ROW_MAPPER);
    }

    public User findById(Connection connection, Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        log.info("[LOG] select user by id");
        return jdbcTemplate.queryForObject(connection, sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(Connection connection, String account) {
        String sql = "select id, account, password, email from users where account = ?";
        log.info("[LOG] select user by account");
        return jdbcTemplate.queryForObject(connection, sql, USER_ROW_MAPPER, account);
    }
}
