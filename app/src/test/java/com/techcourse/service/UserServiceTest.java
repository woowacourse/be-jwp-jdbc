package com.techcourse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        jdbcTemplate.update("DROP TABLE IF EXISTS user_history");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @DisplayName("사용자의 비밀번호를 변경한다.")
    @Test
    void testChangePassword() {
        // given
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(userDao, userHistoryDao);

        // when
        String newPassword = "qqqqq";
        String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        // then
        User actual = userService.getById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @DisplayName("사용자를 찾을 수 없으면 예외가 발생한다.")
    @Test
    void cannotChangePassword() {
        // given
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(userDao, userHistoryDao);

        // when & then
        String newPassword = "newPassword";
        String createBy = "gugu";
        assertThatThrownBy(() -> userService.changePassword(2L, newPassword, createBy))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예외가 발생하면 트랜잭션을 롤백한다.")
    @Test
    void testTransactionRollback() {
        // given
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(userDao, userHistoryDao);

        // when
        String newPassword = "newPassword";
        String createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        // then
        User actual = userService.getById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
