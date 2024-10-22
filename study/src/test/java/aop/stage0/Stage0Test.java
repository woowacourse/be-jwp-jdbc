package aop.stage0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import aop.JdkDynamicAopProxy;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.service.AppUserService;
import aop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class Stage0Test {

    private static final Logger log = LoggerFactory.getLogger(Stage0Test.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserHistoryDao userHistoryDao;

    @Autowired
    private StubUserHistoryDao stubUserHistoryDao;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @BeforeEach
    void setUp() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        UserService userService = new AppUserService(userDao, userHistoryDao);

        JdkDynamicAopProxy jdkDynamicAopProxy = new JdkDynamicAopProxy();
        jdkDynamicAopProxy.setInterfaces(UserService.class);
        jdkDynamicAopProxy.addAdvice(new TransactionHandler(userService, platformTransactionManager));

        UserService proxyService = (UserService) jdkDynamicAopProxy.getProxy();

        String newPassword = "qqqqq";
        String createBy = "gugu";
        proxyService.changePassword(1L, newPassword, createBy);

        User actual = proxyService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        UserService userService = new AppUserService(userDao, stubUserHistoryDao);

        JdkDynamicAopProxy jdkDynamicAopProxy = new JdkDynamicAopProxy();
        jdkDynamicAopProxy.setInterfaces(UserService.class);
        jdkDynamicAopProxy.addAdvice(new TransactionHandler(userService, platformTransactionManager));

        UserService proxy = (UserService) jdkDynamicAopProxy.getProxy();

        String newPassword = "newPassword";
        String createBy = "gugu";
        assertThrows(Exception.class,
                () -> proxy.changePassword(1L, newPassword, createBy));

        User actual = proxy.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
