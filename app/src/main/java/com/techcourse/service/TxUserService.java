package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.TransactionManager;

public class TxUserService implements UserService {

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(long id) {
        return transactionManager.doTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionManager.doTransaction(() -> userService.insert(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.doTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }
}
