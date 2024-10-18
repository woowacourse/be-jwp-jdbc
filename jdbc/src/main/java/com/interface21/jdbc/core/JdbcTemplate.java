package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.ConnectionFailException;
import com.interface21.jdbc.exception.QueryExecutionException;
import com.interface21.jdbc.result.PreparedStatementSetter;
import com.interface21.jdbc.result.ResultSetConverter;
import com.interface21.jdbc.result.RowMapper;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.interface21.jdbc.core.StatementSetter.setStatementsWithPOJOType;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void command(final String sql, final Object... params) {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            executeCommandWithConnection(sql, connection, params);
        } catch (final SQLException exception) {
            throw new ConnectionFailException(sql, exception);
        }
    }

    public void commandWithSetter(final String sql, final PreparedStatementSetter setter) {
        try {
            executeCommandWithConnection(sql, DataSourceUtils.getConnection(dataSource), setter);
        } catch (final SQLException exception) {
            throw new ConnectionFailException(sql, exception);
        }
    }

    private void executeCommandWithConnection(final String sql, final Connection connection, final PreparedStatementSetter setter) throws SQLException {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setter.setValues(pstmt);
            executeCommand(pstmt);
        } finally {
            release(connection);
        }
    }

    private void executeCommandWithConnection(final String sql, final Connection connection, final Object... params) throws SQLException {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            executeCommand(pstmt);
        } finally {
            release(connection);
        }
    }

    private void executeCommand(final PreparedStatement pstmt) {
        try {
            pstmt.executeUpdate();
        } catch (final SQLException exception) {
            throw new QueryExecutionException(pstmt, exception);
        }
    }

    public <T> Stream<T> queryForStream(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return executeQueryTemplate(sql, rowMapper, this::convertToStream, params);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return executeQueryTemplate(sql, rowMapper, this::convertToList, params);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return executeQueryTemplate(sql, rowMapper, this::convertToObject, params);
    }

    private <T, R> R executeQueryTemplate(final String sql, final RowMapper<T> rowMapper, final ResultSetConverter<T, R> converter, final Object... params) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        return executeQueryTemplate(sql, rowMapper, converter, connection, params);
    }

    private <T, R> R executeQueryTemplate(final String sql, final RowMapper<T> rowMapper, final ResultSetConverter<T, R> converter, final Connection connection, final Object... params) {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql);
             final ResultSet resultSet = executeQuery(pstmt, params)) {
            return converter.convert(rowMapper, resultSet);
        } catch (final SQLException exception) {
            throw new ConnectionFailException("연결을 실패했습니다", exception);
        } finally {
            release(connection);
        }
    }

    private ResultSet executeQuery(final PreparedStatement pstmt, final Object... params) throws SQLException {
        setStatementsWithPOJOType(pstmt, params);
        return pstmt.executeQuery();
    }

    private <T> T convertToObject(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        try (resultSet) {
            if (resultSet.next()) {
                return rowMapper.mapToRow(resultSet);
            }
            return null;
        }
    }

    private <T> Stream<T> convertToStream(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        return convertToList(rowMapper, resultSet).stream();
    }

    private <T> List<T> convertToList(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final List<T> list = new ArrayList<>();
        try (resultSet) {
            while (resultSet.next()) {
                list.add(rowMapper.mapToRow(resultSet));
            }
        }
        return list;
    }

    private void release(final Connection connection) {
        if (TransactionSynchronizationManager.isActiveTransaction()) {
            return;
        }
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
