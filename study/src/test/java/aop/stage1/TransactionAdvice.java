package aop.stage1;

import aop.DataAccessException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(final PlatformTransactionManager platformTransactionManager) {
        this.transactionManager = platformTransactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Object result = null;
        try {
            result = invocation.proceed();
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
        return result;
    }
}
