package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (resultSet, rowNum) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public User findById(final Long id) {
//        final var sql = "select id, account, password, email from users where id = ?";
//
//        Connection conn = null;
//        PreparedStatement pstmt = null;
//        ResultSet rs = null;
//        try {
//            conn = dataSource.getConnection();
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setLong(1, id);
//            rs = pstmt.executeQuery();
//
//            log.debug("query : {}", sql);
//
//            if (rs.next()) {
//                return new User(
//                        rs.getLong(1),
//                        rs.getString(2),
//                        rs.getString(3),
//                        rs.getString(4));
//            }
//            return null;
//        } catch (SQLException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                if (rs != null) {
//                    rs.close();
//                }
//            } catch (SQLException ignored) {}
//
//            try {
//                if (pstmt != null) {
//                    pstmt.close();
//                }
//            } catch (SQLException ignored) {}
//
//            try {
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException ignored) {}
//        }
        return null;
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
