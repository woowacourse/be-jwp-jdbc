package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.exception.UndeclaredThrowableException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Nullable
    public <T> T executeWithTransaction(final Supplier<T> supplier) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            final T result = supplier.get();
            connection.commit();
            return result;
        } catch (final RuntimeException e) {
            if (connection != null) {
                rollback(connection);
            }
            throw e;
        } catch (final Throwable e) {
            if (connection != null) {
                rollback(connection);
            }
            throw new UndeclaredThrowableException(e);
        } finally {
            if (connection != null) {
                closeTransactional(connection);
            }
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void closeTransactional(final Connection connection) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection);
    }

    public void executeWithoutResult(final Runnable action) {
        executeWithTransaction(() -> {
            action.run();
            return null;
        });
    }
}
