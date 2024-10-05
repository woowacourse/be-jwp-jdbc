package com.techcourse.dao;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        PreparedStatementCallBack callBack = (pstmt) -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };

        templateCommend(sql, callBack);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        PreparedStatementCallBack callBack = (pstmt) -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };

        templateCommend(sql, callBack);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users;";

        return templateQuery(sql, (rs) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4))
        );
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return templateQueryOne(sql, (rs) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)), id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return templateQueryOne(sql, (rs) -> new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)),
                account
        );
    }

    private <T> T templateQueryOne(String sql, ResultSetCallBack<T> callBack, Object... args) {
        log.debug("query : {}", sql);
        try (var connection = dataSource.getConnection(); var pstmt = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                T result = callBack.callback(rs);
                rs.close();
                return result;
            }

            rs.close();
        } catch (Exception ignored) {
        }

        return null;
    }

    private <T> List<T> templateQuery(String sql, ResultSetCallBack<T> callBack, Object... args) {
        log.debug("query : {}", sql);
        List<T> results = new ArrayList<>();

        try (var connection = dataSource.getConnection(); var pstmt = connection.prepareStatement(sql)) {
            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(callBack.callback(rs));
            }

            rs.close();
        } catch (Exception ignored) {
        }

        return results;
    }

    // delete, uddate, insert
    // 인자가 필요한 버전, 인자가 필요하지 않은 버전
    private void templateCommend(String sql, PreparedStatementCallBack callBack) {
        log.debug("query : {}", sql);
        try (var connection = dataSource.getConnection(); var pstmt = connection.prepareStatement(sql)) {
            callBack.callback(pstmt);
            pstmt.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    private interface PreparedStatementCallBack {

        void callback(PreparedStatement preparedStatement) throws SQLException;
    }

    private interface ResultSetCallBack<T> {

        T callback(ResultSet resultSet) throws SQLException;
    }
}
