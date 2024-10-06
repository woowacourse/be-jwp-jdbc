package com.techcourse.dao;

import com.interface21.jdbc.core.Parameters;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final var parameters = new Parameters();
        parameters.add(1, user.getAccount());
        parameters.add(2, user.getPassword());
        parameters.add(3, user.getEmail());

        jdbcTemplate.update(sql, parameters);
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        final var parameters = new Parameters();
        parameters.add(1, id);

        RowMapper<User> userRowMapper = (resultSet) -> new User(
			resultSet.getLong(1),
			resultSet.getString(2),
			resultSet.getString(3),
			resultSet.getString(4)
		);
        return (User) jdbcTemplate.queryForObject(sql, parameters, userRowMapper);
    }



    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
