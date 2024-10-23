package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T executeMethodWithTransaction(Supplier<T> action) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            T result = action.get();
            connection.commit();

            return result;

        } catch (SQLException e) {
            throw new DataAccessException(e);

        } catch(RuntimeException e) {
            rollback(connection);
            throw e;

        }finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void executeMethodWithTransaction(Runnable action) {
        executeMethodWithTransaction(() -> {
            action.run();
            return null;
        });
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();

        } catch (SQLException e) {
            log.error("Rollback failed", e.getMessage());
            throw new DataAccessException(e);
        }
    }
}
