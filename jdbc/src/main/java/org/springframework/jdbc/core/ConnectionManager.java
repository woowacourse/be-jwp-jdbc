package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private final DataSource dataSource;

    public ConnectionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        if (TransactionContext.isEmpty()) {
            return getNewConnection();
        }
        return TransactionContext.get();
    }

    private Connection getNewConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void closeNotTransactional(Connection connection) {
        if (isTransactional(connection)) {
            return;
        }
        close(connection);
    }

    private boolean isTransactional(final Connection connection) {
        if (TransactionContext.isEmpty()) {
            return false;
        }
        return TransactionContext.get() == (connection);
    }

    private void close(final Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
