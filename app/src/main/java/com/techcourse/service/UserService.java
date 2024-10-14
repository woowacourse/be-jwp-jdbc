package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            changePasswordWithTransaction(id, newPassword, createBy);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void changePasswordWithTransaction(final long id, final String newPassword,
                                               final String createBy) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);

        try {
            User user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit();
        } catch (Throwable e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
