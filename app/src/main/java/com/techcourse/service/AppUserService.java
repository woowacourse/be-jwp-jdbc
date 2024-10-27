package com.techcourse.service;

import com.interface21.transaction.manager.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 id인 user가 존재하지 않습니다."));
    }

    @Override
    public User findByAccount(String account) {
        return userDao.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("해당 account인 user가 존재하지 않습니다."));
    }

    @Override
    public void save(User user) {
        TransactionManager.runTransaction(() -> userDao.insert(user), DataSourceConfig.getInstance());
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        TransactionManager.runTransaction(() -> {
                    userDao.update(user);
                    userHistoryDao.log(new UserHistory(user, createBy));
                },
                DataSourceConfig.getInstance());
    }
}
