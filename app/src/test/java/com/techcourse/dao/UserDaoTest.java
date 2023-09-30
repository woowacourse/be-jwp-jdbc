package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
    }

    @Test
    void findAll() {
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        userDao.insert(new User("gugu2", "password2", "hkkang2@woowahan.com"));

        final var users = userDao.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void findById() {
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        final var user = userDao.findById(1L).get();

        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));
        final var user = userDao.findByAccount(account).get();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccount_MultipleResultsForAccount_ExceptionThrown() {
        final var account = "gugu";
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));
        assertThatThrownBy(() -> userDao.findByAccount(account))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(1L).get();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        final var newPassword = "password99";
        final var user = userDao.findById(1L).get();
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
