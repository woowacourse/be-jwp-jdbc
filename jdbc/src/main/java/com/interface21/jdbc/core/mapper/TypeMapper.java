package com.interface21.jdbc.core.mapper;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class TypeMapper extends PreparedStatementMapper {

    public TypeMapper(PreparedStatement preparedStatement, Object[] params, JDBCType[] types) {
        super(preparedStatement, params, types);
    }

    @Override
    protected void setStatement() throws SQLException {
        for (int i = 0; i < jdbcType.length; i++) {
            preparedStatement.setObject(i + DB_INDEX_OFFSET, params[i], jdbcType[i]);
        }
    }
}
