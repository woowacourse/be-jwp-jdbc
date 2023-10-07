package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.JdbcUserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.repository.UserRepository;
import org.springframework.jdbc.transaction.TransactionManager;
import org.springframework.jdbc.transaction.Transactional;

import javax.sql.DataSource;

public class UserService {

    private final Transactional transactional;
    private final UserRepository userRepository;
    private final JdbcUserHistoryDao jdbcUserHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final JdbcUserHistoryDao jdbcUserHistoryDao) {
        this.transactional = new Transactional(dataSource);
        this.userRepository = new UserRepository(userDao);
        this.jdbcUserHistoryDao = jdbcUserHistoryDao;
    }

    public User findById(final long id) {
        return userRepository.findById(id);
    }

    public void insert(final User user) {
        userRepository.save(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactional.execute(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userRepository.update(user);
            jdbcUserHistoryDao.log(new UserHistory(user, createBy));
            return null;
        });
    }
}
