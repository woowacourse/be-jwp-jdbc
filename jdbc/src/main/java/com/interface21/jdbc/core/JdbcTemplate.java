package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.ResultNotSingleException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void write(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public void write(Connection connection, String sql, Object... params) {
        execute(connection, sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> readAll(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> mapResultSetToList(rowMapper, pstmt), params);
    }

    public <T> List<T> readAll(Connection connection, String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(connection, sql, pstmt -> mapResultSetToList(rowMapper, pstmt), params);
    }

    private <T> List<T> mapResultSetToList(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.rowMap(resultSet));
            }

            return result;
        }
    }

    public <T> T read(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> result = readAll(sql, rowMapper, params);
        if (result.size() > 1) {
            throw new ResultNotSingleException(result.size());
        }

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    public <T> T read(Connection connection, String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> result = readAll(connection, sql, rowMapper, params);
        if (result.size() > 1) {
            throw new ResultNotSingleException(result.size());
        }

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor, Object... params) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            PreparedStatementSetter.setParameters(pstmt, params);
            return executor.execute(pstmt);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T execute(Connection connection, String sql, PreparedStatementExecutor<T> executor, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            PreparedStatementSetter.setParameters(pstmt, params);
            return executor.execute(pstmt);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
