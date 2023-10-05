package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementCallback<T> {

    T doInStatement(PreparedStatement stmt) throws SQLException;
}
