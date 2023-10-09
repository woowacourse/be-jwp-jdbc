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

    private final PlatformTransactionManager platformTransactionManager;

    public TransactionAdvice(final PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
//            Object[] arguments = invocation.getArguments();
//            Class<? extends MethodInvocation> aClass = invocation.getClass();
//            Object result = invocation.getMethod().invoke(aClass, arguments);

            //Advice(추가 기능)를 적용한 후, 원본 메서드를 호출하고 이를 실행하는 역할을 한다.
            Object result = invocation.proceed();
            platformTransactionManager.commit(status);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException();
        }

    }
}
