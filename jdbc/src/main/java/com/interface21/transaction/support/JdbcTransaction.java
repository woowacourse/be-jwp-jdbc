package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import com.interface21.jdbc.support.h2.H2SQLExceptionTranslator;

public class JdbcTransaction {

    private final Connection connection;
    private final H2SQLExceptionTranslator exceptionTranslator;

    public JdbcTransaction(Connection connection, H2SQLExceptionTranslator exceptionTranslator) {
        this.connection = connection;
        this.exceptionTranslator = exceptionTranslator;
    }

    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
