package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.info("sql : {}", sql);
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ? , password = ?, email = ?";
        log.info("sql : {}", sql);
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        log.info("sql : {}", sql);
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        log.info("sql : {}", sql);
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        log.info("sql : {}", sql);
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, account);
    }
}
