package org.springframework.transaction.support;

import org.springframework.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isInTransaction = ThreadLocal.withInitial(() -> false);

    private TransactionSynchronizationManager() {}

    public static Connection getConnection(DataSource dataSource) {
        Map<DataSource, Connection> dataSourceAndConnection = resources.get();
        if (dataSourceAndConnection == null || dataSourceAndConnection.get(dataSource) == null) {
            throw new DataAccessException("DataSource에 binding된 Connection이 없습니다.");
        }

        return dataSourceAndConnection.get(dataSource);
    }

    public static void bindConnection(DataSource dataSource, Connection connection) {
        if (resources.get() == null) {
            Map<DataSource, Connection> threadResource = new HashMap<>();
            resources.set(threadResource);
        }

        resources.get().put(dataSource, connection);
    }

    public static Connection unbindConnection(DataSource dataSource) {
        if (resources.get() == null || !resources.get().containsKey(dataSource)) {
            throw new IllegalArgumentException("등록된 DataSource가 없습니다.");
        }

        Map<DataSource, Connection> resource = resources.get();
        Connection connection = resource.get(dataSource);
        resources.remove();
        return connection;
    }

    public static boolean isInTransaction() {
        return isInTransaction.get();
    }

    public static void setInTransaction(boolean inTransaction) {
        if (inTransaction && isInTransaction()) {
            throw new IllegalStateException("이미 트랜잭션이 진행중입니다.");
        }
        isInTransaction.remove();
        isInTransaction.set(inTransaction);
    }
}
