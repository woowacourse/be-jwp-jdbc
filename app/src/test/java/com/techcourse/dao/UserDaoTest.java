package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user).isPresent();
        assertThat(user.get().getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "ocean";
        userDao.insert(new User(account, "password", "donghae1999@gmail.com"));
        final var user = userDao.findByAccount(account);

        assertThat(user).isPresent();
        assertThat(user.get().getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual).isPresent();
        assertThat(actual.get().getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).orElseThrow();
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L).orElseThrow();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
