package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> rowMapper = (resultSet) -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
    );

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    public void insert(final User user) {
        String sql = String.format(
                "insert into users (account, password, email) values ('%s', '%s', '%s')",
                user.getAccount(), user.getPassword(), user.getEmail());

        jdbcTemplate.update(sql);
    }

    public void update(final User user) {
        var sql = String.format("""
                update 
                    users 
                set
                    account = '%s',
                    password = '%s', 
                    email = '%s'
                where 
                    id = %d
                """, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());

        jdbcTemplate.update(sql);
    }

    public List<User> findAll() {
        var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, rowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = " + id;

        return jdbcTemplate.queryForObject(sql, rowMapper);
    }

    public User findByAccount(final String account) {
        var sql = "select id, account, password, email from users where account = '" + account+"'";

        return jdbcTemplate.queryForObject(sql, rowMapper);
    }
}
