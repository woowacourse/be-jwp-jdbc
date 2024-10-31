package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.SqlExecutionException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        log.debug("update Executing SQL: {}", sql);

        return executeQuery(sql, setParameter(params), PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        log.debug("queryForObject Executing SQL: {}", sql);

        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameter(params).setValue(ps);
            final ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        } catch (final SQLException e) {
            throw new SqlExecutionException(e.getMessage(), e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        log.debug("queryForList Executing SQL: {}", sql);

        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet resultSet = ps.executeQuery()) {

            setParameter(params).setValue(ps);
            final List<T> values = new ArrayList<>();
            while (resultSet.next()) {
                values.add(rowMapper.mapRow(resultSet));
            }
            return values;
        } catch (final SQLException e) {
            throw new SqlExecutionException(e.getMessage(), e);
        }
    }

    private <T> T executeQuery(final String sql, final PreparedStatementSetter pss, final SqlExecutor<T> executor) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            pss.setValue(ps);
            return executor.executor(ps);
        } catch (final SQLException e) {
            throw new SqlExecutionException(e.getMessage());
        }
    }

    private PreparedStatementSetter setParameter(final Object... params) {
        return ps -> {
            int index = 1;
            for (final Object param : params) {
                ps.setObject(index++, param);
            }
        };
    }
}
