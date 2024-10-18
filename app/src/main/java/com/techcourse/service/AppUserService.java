package com.techcourse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class AppUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public AppUserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        log.info("[AppUserService] changePassword: id={}, newPassword={}, createBy={}", id, newPassword, createBy);
        final User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.create(new UserHistory(user, createBy));
    }
}
