package aop.stage1;

import aop.DataAccessException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice  implements MethodInterceptor {
    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Object result = null;
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            result = invocation.proceed();
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
        transactionManager.commit(status);
        return result;
    }
}
