package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        if (isConnectionOpen(connection)) {
            return connection;
        }

        try {
            connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    private static boolean isConnectionOpen(Connection connection) {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to check if the JDBC Connection is closed", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            doReleaseConnection(connection, dataSource);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }

    public static void doReleaseConnection(Connection connection, DataSource dataSource) throws SQLException {
        if (!connection.getAutoCommit()) {
            return;
        }

        TransactionSynchronizationManager.unbindResource(dataSource);
        connection.close();
    }
}
