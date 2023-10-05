package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.transaction.TransactionExecutor;
import org.springframework.transaction.TransactionManager;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(final TransactionManager transactionManager, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionExecutor.execute(transactionManager, () -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(transactionManager.getConnection(), user);
            userHistoryDao.log(transactionManager.getConnection(), new UserHistory(user, createBy));
        });
    }
}
