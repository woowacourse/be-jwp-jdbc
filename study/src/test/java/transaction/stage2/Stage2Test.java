package transaction.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 트랜잭션 전파(Transaction Propagation)란? 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 * <p>
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다. SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
 * <p>
 * https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-propagation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage2Test {

    private static final Logger log = LoggerFactory.getLogger(Stage2Test.class);

    @Autowired
    private FirstUserService firstUserService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    /*
    생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까?

    1개
    Propagation : REQUIRED -> REQUIRED
    */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /*
    생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까?

    2개
    Propagation : REQUIRED -> REQUIRES_NEW
    */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew"
                );
    }

    /*
    firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
    REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는지 확인해보자.

    Propagation : REQUIRED -> REQUIRES_NEW
    두 번째 트랜잭션은 롤백이 되지 않는다
    */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /*
    FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
    주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.

    주석 유지: X -> SUPPORTS
    1, saveSecondTransactionWithSupports

    주석 해제: REQUIRED -> SUPPORTS
    1, saveFirstTransactionWithSupports
    */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
    }

    /*
    FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
    주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자. SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.

    주석 유지: X -> MANDATORY
    IllegalTransactionStateException: No existing transaction found for transaction marked with propagation 'mandatory'

    주석 해제: REQUIRED -> MANDATORY
    1, saveFirstTransactionWithMandatory
    */
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /*
    아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
    다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
    스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.

    주석 유지: X -> NOT_SUPPORTED
    null is Actual Transaction Active : ❌ false
    transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false

    주석 해제: REQUIRED -> NOT_SUPPORTED
    transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported is Actual Transaction Active : ✅ true
    transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
    */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported"
                );
    }

    /*
    아래 테스트는 왜 실패할까? FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?

    주석 유지: X -> NESTED
    null is Actual Transaction Active : ❌ false
    transaction.stage2.SecondUserService.saveSecondTransactionWithNested is Actual Transaction Active : ✅ true


    주석 해제: REQUIRED -> NESTED
    transaction.stage2.FirstUserService.saveFirstTransactionWithNested is Actual Transaction Active : ✅ true
    org.springframework.transaction.NestedTransactionNotSupportedException: JpaDialect does not support savepoints - check your JPA provider's capabilities
    */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /*
    마찬가지로 @Transactional을 주석처리하면서 관찰해보자.

    주석 유지: X -> NEVER
    null is Actual Transaction Active : ❌ false
    transaction.stage2.SecondUserService.saveSecondTransactionWithNever is Actual Transaction Active : ❌ false

    주석 해제: REQUIRED -> NEVER
    transaction.stage2.FirstUserService.saveFirstTransactionWithNever is Actual Transaction Active : ✅ true
    org.springframework.transaction.IllegalTransactionStateException: Existing transaction found for transaction marked with propagation 'never'
    */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
