package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface JdbcCallback<T> {
    T execute(PreparedStatement preparedStatement) throws SQLException;
}
