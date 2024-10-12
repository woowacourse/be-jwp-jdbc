package transaction.stage2;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class FirstUserService {

    private static final Logger log = LoggerFactory.getLogger(FirstUserService.class);

    private final UserRepository userRepository;
    private final SecondUserService secondUserService;

    @Autowired
    public FirstUserService(final UserRepository userRepository,
                            final SecondUserService secondUserService) {
        this.userRepository = userRepository;
        this.secondUserService = secondUserService;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithRequired() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithRequired();

        return of(firstTransactionName, secondTransactionName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithRequiredNew() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithRequiresNew();

        return of(firstTransactionName, secondTransactionName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveAndExceptionWithRequiredNew() {
        secondUserService.saveSecondTransactionWithRequiresNew();

        userRepository.save(User.createTest());
        logActualTransactionActive();

        throw new RuntimeException();
    }

//    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithSupports() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithSupports();

        return of(firstTransactionName, secondTransactionName);
    }

//    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithMandatory() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithMandatory();

        return of(firstTransactionName, secondTransactionName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithNotSupported() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithNotSupported();

        return of(firstTransactionName, secondTransactionName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithNested() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithNested();

        return of(firstTransactionName, secondTransactionName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Set<String> saveFirstTransactionWithNever() {
        final var firstTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        userRepository.save(User.createTest());
        logActualTransactionActive();

        final var secondTransactionName = secondUserService.saveSecondTransactionWithNever();

        return of(firstTransactionName, secondTransactionName);
    }

    // Set은 중복을 허용하지 않기 때문에, 부모 트랜잭션을 사용하는 경우 size는 1이 됨
    // transactionName은 {패키지 경로를 포함한 클래스명}.{메서드명}
    private Set<String> of(final String firstTransactionName, final String secondTransactionName) {
        return Stream.of(firstTransactionName, secondTransactionName)
                .filter(transactionName -> !Objects.isNull(transactionName))
                .collect(Collectors.toSet());
    }

    private void logActualTransactionActive() {
        final var currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        final var actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        final var emoji = actualTransactionActive ? "✅" : "❌";
        log.info("\n{} is Actual Transaction Active : {} {}", currentTransactionName, emoji, actualTransactionActive);
    }
}
