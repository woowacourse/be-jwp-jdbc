package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> resource = resources.get();
        return resource.get(key);
    }

    public static void bindResource(final DataSource key,
                                    final Connection value) {
        final Map<DataSource, Connection> resource = resources.get();
        resource.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> resource = resources.get();
        return resource.remove(key);
    }
}
