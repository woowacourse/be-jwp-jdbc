package transaction.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.NestedTransactionNotSupportedException;

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

    /**
     * 생성된 트랜잭션이 몇 개인가? 1 왜 그런 결과가 나왔을까? Propagation.REQUIRED는 기존 트랜잭션이 없으면 새로운 트랜잭션을 만들고, 기존 트랜잭션이 있으면 참여하기 때문에
     * saveFirstTransactionWithRequired()에서 하나 만들고 saveSecondTransactionWithRequired()는 이전에 만든거 참여한다.
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * 생성된 트랜잭션이 몇 개인가? 2 왜 그런 결과가 나왔을까? Propagation.REQUIRES_NEW는 무조건 새로 생성하기 때문
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다. REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는
     * 지 확인해보자.: saveAndExceptionWithRequiredNew()는 롤백하고 그 안에 saveSecondTransactionWithRequiresNew()는 커밋된다.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).isEmpty();

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다. 주석인 상태에서 테스트를 실행했을 때와 주석을
     * 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자. Propagation.SUPPORTS는 현재 트랜잭션이 있으면 거기 참여하고 없다면 없는데로 산다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다. 주석인 상태에서 테스트를 실행했을 때와
     * 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자. SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자. 기존 트랜잭션이 없으면 예외가 발생한다. 있다면 참여한다.
     */
    @Test
    void testMandatory() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
                .isInstanceOf(IllegalTransactionStateException.class);
//        final var actual = firstUserService.saveFirstTransactionWithMandatory();
//
//        log.info("transactions : {}", actual);
//        assertThat(actual)
//                .hasSize(1)
//                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석
     * 처리하자. 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자. 걍 트랜잭션 없이 진행한다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까? FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * 기존 트랜잭션이 없다면 새로운 트랜잭션을 만들고, 있다면 중첩 트랜잭션을 만든다.
     * 중첩 트랜잭션은 외부 트랜잭션의 영향을 받지만, 외부에 영향을 주지는 않는다.
     * 중첩 트랜잭션이 롤백 되어도 외부 트랜잭션은 커밋할 수 있다.
     * 외부 트랜잭션이 롤백되면 중첩 트랜잭션도 함께 롤백된다.
     *
     * JPA에서는 지원하지 않음.
     */
    @Test
    void testNested() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNested())
                .isInstanceOf(NestedTransactionNotSupportedException.class);
//        final var actual = firstUserService.saveFirstTransactionWithNested();

//        log.info("transactions : {}", actual);
//        assertThat(actual)
//                .hasSize(2)
//                .containsExactly("");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     *
     * 기존 트랜잭션이 없다면 없이 진행, 기존 트랜잭션이 있다면 예외 발생
     */
    @Test
    void testNever() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNever())
                .isInstanceOf(IllegalTransactionStateException.class);
//        final var actual = firstUserService.saveFirstTransactionWithNever();
//
//        log.info("transactions : {}", actual);
//        assertThat(actual)
//                .hasSize(0)
//                .containsExactly("");
    }
}
