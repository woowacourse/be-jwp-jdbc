package com.techcourse.service;

import static com.techcourse.fixture.UserFixture.GUGU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.dao.rowmapper.UserRowMapper;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private TransactionManager transactionManager;
    private UserRowMapper userRowMapper;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.transactionManager = new TransactionManager(DataSourceConfig.getInstance());
        this.userRowMapper = new UserRowMapper();
        this.userDao = new UserDao(jdbcTemplate, userRowMapper);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao.insert(GUGU.user());
    }

    @Test
    void testChangePassword() {
        final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final TxUserService userService = new TxUserService(transactionManager, appUserService);

        final String newPassword = "qqqqq";
        final String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final User actual = userService.getById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final TxUserService userService = new TxUserService(transactionManager, appUserService);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.getById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
