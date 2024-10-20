package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        if (isActualTransactionActive()) {
            return connections.get(key);
        }

        return null;
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = resources.get();
        connections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        if (isActualTransactionActive()) {
            return connections.remove(key);
        }

        return null;
    }

    public static boolean isActualTransactionActive() {
        Map<DataSource, Connection> connections = resources.get();

        return !connections.isEmpty();
    }
}
