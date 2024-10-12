package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    public static final String INSERT_QUERY = "insert into users (account, password, email) values (?, ?, ?)";
    public static final String UPDATE_QUERY = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
    public static final String SELECT_ALL_QUERY = "select * from users";
    public static final String SELECT_BY_ID_QUERY = "select * from users where id = ?";
    public static final String SELECT_BY_ACCOUNT_QUERY = "select * from users where account = ?";

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> {
        if (rs.next()) {
            return new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );
        }
        return null;
    };

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.command(INSERT_QUERY, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.command(UPDATE_QUERY, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.query(USER_ROW_MAPPER, SELECT_ALL_QUERY);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject(USER_ROW_MAPPER, SELECT_BY_ID_QUERY, id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.queryForObject(USER_ROW_MAPPER, SELECT_BY_ACCOUNT_QUERY, account);
    }
}
