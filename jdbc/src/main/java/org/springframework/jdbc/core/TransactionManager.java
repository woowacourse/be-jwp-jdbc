package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final Callable<T> callable) {
        return executeTemplate(()-> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void execute(final Runnable runnable) {
        executeTemplate(()-> {
            runnable.run();
            return null;
        });
    }

    private <T> T executeTemplate(final Supplier<T> supplier) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            final T result = supplier.get();
            connection.commit();
            return result;
        } catch (SQLException | DataAccessException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
