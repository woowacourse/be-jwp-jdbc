package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.jdbc.core.PreparedStatementUtils.getPreparedStatement;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int VALID_RESULT_COUNT = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... obj) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement pstmt = getPreparedStatement(sql, obj, conn)) {
            log.debug("query : {}", sql);
            pstmt.execute();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... obj) {
        final List<T> result = tryCatchTemplate(rs -> {
            log.debug("query : {}", sql);
            return convertResultSetToInstances(rowMapper, rs);
        }, sql, obj);
        validateResultSetSize(result);
        return Optional.of(result.get(0));
    }

    public <T> List<T> tryCatchTemplate(final StatementExecutor<List<T>> executor, final String sql, final Object... obj) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement pstmt = getPreparedStatement(sql, obj, conn);
             final ResultSet rs = pstmt.executeQuery()) {
            return executor.execute(rs);
        } catch (final SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    private <T> void validateResultSetSize(List<T> result) {
        if (result.size() > VALID_RESULT_COUNT) {
            throw new DataAccessException("조회하려는 레코드는 2개 이상일 수 없습니다.");
        }
    }

    private <T> List<T> convertResultSetToInstances(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return result;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... obj) {
        return tryCatchTemplate(rs -> {
            log.debug("query : {}", sql);
            return convertResultSetToInstances(rowMapper, rs);
        }, sql, obj);
    }
}
