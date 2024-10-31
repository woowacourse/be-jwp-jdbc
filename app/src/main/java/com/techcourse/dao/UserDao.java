package com.techcourse.dao;

import com.interface21.jdbc.core.Parameters;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> userRowMapper = (resultSet) -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, () -> {
            final var parameters = new Parameters();
            parameters.add(1, user.getAccount());
            parameters.add(2, user.getPassword());
            parameters.add(3, user.getEmail());

            return parameters;
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, () -> {
            final var parameters = new Parameters();
            parameters.add(1, user.getAccount());
            parameters.add(2, user.getPassword());
            parameters.add(3, user.getEmail());
            parameters.add(4, user.getId());

            return parameters;
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, Parameters::new, userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, () -> {
            final var parameters = new Parameters();
            parameters.add(1, id);

            return parameters;
        }, userRowMapper);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, () -> {
            final var parameters = new Parameters();
            parameters.add(1, account);

            return parameters;
        }, userRowMapper);
    }
}
