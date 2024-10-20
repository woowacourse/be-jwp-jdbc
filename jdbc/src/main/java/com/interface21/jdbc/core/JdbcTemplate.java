package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.datasource.ConnectionContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, @Nullable Object... args) throws DataAccessException {
        Connection conn = getConnection();
        final PreparedStatement pstmt = getPreparedStatement(sql, conn);

        try (pstmt) {
            setPreparedStatementArgs(sql, args, pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object... args) throws DataAccessException {
        Connection conn = getConnection();
        final PreparedStatement pstmt = getPreparedStatement(sql, conn);

        try (pstmt) {
            setPreparedStatementArgs(sql, args, pstmt);
            return executeQuery(rowMapper, pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args)
            throws DataAccessException {
        Connection conn = getConnection();
        final PreparedStatement pstmt = getPreparedStatement(sql, conn);

        try (pstmt) {
            setPreparedStatementArgs(sql, args, pstmt);
            List<T> result = executeQuery(rowMapper, pstmt);
            return requiredSingleResult(result);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatementArgs(String sql, Object[] args, PreparedStatement pstmt) throws SQLException {
        log.debug("query = {}", sql);
        PreparedStatementSetter pss = createArgsPreparedStatementSetter(args);
        pss.setValues(pstmt);
    }

    private <T> List<T> executeQuery(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            T object = rowMapper.mapRow(rs, rs.getRow());
            result.add(object);
        }

        return result;
    }

    private <T> T requiredSingleResult(List<T> result) {
        if (result.isEmpty()) {
            throw new EmptyResultDataAccessException("데이터 개수가 0개입니다.");
        }
        if (result.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("데이터 개수가 올바르지 않습니다. (size: %d)".formatted(result.size()));
        }
        return result.get(0);
    }

    public PreparedStatementSetter createArgsPreparedStatementSetter(Object[] args) {
        return (pstmt) -> {
            for (int idx = 1; idx <= args.length; idx++) {
                pstmt.setObject(idx, args[idx - 1]);
            }
        };
    }

    private PreparedStatement getPreparedStatement(String sql, Connection conn) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Connection getConnection() {
        try {
            com.interface21.jdbc.datasource.Connection conn = ConnectionContext.conn.get();
            if(conn.isClosed()) {
                ConnectionContext.conn.remove();
                throw new NullPointerException();
            }
            return conn.getConnection();
        } catch (NullPointerException nullPointerException) {
            return createNewConnection();
        }
    }

    private Connection createNewConnection() {
        try {
            Connection conn = dataSource.getConnection();
            ConnectionContext.conn.set(new com.interface21.jdbc.datasource.Connection(conn));

            return conn;
        } catch (SQLException sqlException) {
            throw new DataAccessException(sqlException);
        }
    }
}
