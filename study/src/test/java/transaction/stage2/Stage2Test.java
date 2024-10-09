package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 전파(Transaction Propagation)란?
 * 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 *
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다.
 * SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
 *
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

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);

        /**
         * Propagation.REQUIRED은 트랜잭션이 존재하지 않으면 생성한다.
         * firstUserService에서 첫번째 트랜잭션을 생성했고 secondUserService은 fistUserService의 트랜잭션에 참여한다.
         * */
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);

        /**
         * REQUIRES_NEW는 새로운 트랜잭션을 만들고 기존에 존재하던 트랜잭션은 중지시킨다.
         */
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        /**
         * 첫 번째 트랜잭션은 롤백되었지만 두 번째 트랜잭션은 예외가 발생하지 않고 종료됐다.
         */
        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        /**
         * 주석 상태 : 외부 트랜잭션이 없으면 트랜잭션 없이 실행된다. 물리적 트랜잭션이 실행되지 않는다.
         * 주석 해제 : 외부 트랜잭션이 있으면 참여한다. 물리적 트랜잭션이 실행된다.
         */
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     */
    @Test
    void testMandatory() {
        /**
         * Propagation.MANDATORY은 현재 트랜잭션이 없으면 예외를 발생시킨다.
         * SUPPORTS와는 트랜잭션이 없어도 그냥 메서드를 실행한다는 점에서 차이가 있다.
         * 주석 상태 : 아래의 테스트를 통과한다.
         * assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
         *                 .isInstanceOf(IllegalTransactionStateException.class);
         */

        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    @Test
    void testNotSupported() {
        /**
         * 주석 해제 : 1개의 물리적 트랜잭션(FirstUserService.saveFirstTransactionWithNotSupported)이 동작한다.
         * 주석 상태 : NOT_SUPPORTED는 트랜잭션없이 동작하므로 물리적 트랜잭션이 동작하지 않는다.
         * 물리적 트랜잭션 : 데이터베이스 커넥션을 통해 우리의 쿼리가 실제 커넥션을 통해 커밋/롤백 하는 역할
         * 논리적 트랜잭션 : A라는 트랜잭션에 B 라는 트랜잭션이 참가하는 경우. 즉, 하나의 트랜잭션 내부에 다른 트랜잭션이 추가로 사용하는 경우 이 트랜잭션들을 논리 트랜잭션이라고 한다.
         * 논리적 트랜잭션은 스프링의 트랜잭션 관리 메커니즘인 PlatformTransactionManager에 의해 관리된다.
         */
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     */
    @Test
    void testNested() {
        /**
         * NESTED는 이미 트랜잭션이 시작되어있는 상태에서 내부에 새로운 트랜잭션 경계를 설정하고자 할 때 사용하는 속성이다.
         * JPA를 사용하는 경우 중첩된 트랜잭션 경계를 설정할 수 없어 지원하지 않는다.
         */
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     */
    @Test
    void testNever() {
        /**
         * NEVER는 트랜잭션이 존재하는 경우 예외를 발생시켜 트랜잭션을 사용하지 않는 것을 강제하는 속성이다.
         */
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
