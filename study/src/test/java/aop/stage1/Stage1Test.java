package aop.stage1;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage1Test {

    private static final Logger log = LoggerFactory.getLogger(Stage1Test.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserHistoryDao userHistoryDao;

    @Autowired
    private StubUserHistoryDao stubUserHistoryDao;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private ProxyFactoryBean proxyFactoryBean;

    @BeforeEach
    void setUp() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        proxyFactoryBean = new ProxyFactoryBean();

        //실제로 다 생성해서 넣어주고 빈 등록까지 하기 때문에 setProxyTargetClass(true)안 해줘도 됨
        TransactionPointcut pointcut = new TransactionPointcut();
        TransactionAdvice advice = new TransactionAdvice(platformTransactionManager);
        proxyFactoryBean.addAdvisor(new TransactionAdvisor(pointcut, advice));
    }

    @Test
    void testChangePassword() {
        UserService userService = new UserService(userDao, userHistoryDao);
        proxyFactoryBean.setTarget(userService);

        userService = (UserService) proxyFactoryBean.getObject();

        String newPassword = "qqqqq";
        String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        proxyFactoryBean.setTarget(new UserService(userDao, stubUserHistoryDao));

        UserService userService = (UserService) proxyFactoryBean.getObject();

        String newPassword = "newPassword";
        String createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
