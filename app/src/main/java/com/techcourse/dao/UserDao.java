package com.techcourse.dao;

import javax.sql.DataSource;
import java.util.List;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final RowMapper<User> rowMapper =
            rs -> new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );
    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, pss -> {
            pss.setString(1, user.getAccount());
            pss.setString(2, user.getPassword());
            pss.setString(3, user.getEmail());
        });
    }

    public void update(User user) {
        String sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.update(sql, pss -> {
            pss.setString(1, user.getAccount());
            pss.setString(2, user.getPassword());
            pss.setString(3, user.getEmail());
            pss.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, rowMapper, pss -> {
        });
    }

    public User findById(Long id) {
        String sql = "select * from users where id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, pss -> pss.setLong(1, id));
    }

    public User findByAccount(String account) {
        String sql = "select * from users where account=?";
        return jdbcTemplate.queryForObject(sql, rowMapper, pss -> pss.setString(1, account));
    }
}
