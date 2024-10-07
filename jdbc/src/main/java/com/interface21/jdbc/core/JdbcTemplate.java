package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, @Nullable Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setPreparedStatementParameter(args, pstmt);
            log.info("query = {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setPreparedStatementParameter(args, pstmt);
            log.info("query = {}", sql);

            return getQueryResult(rowMapper, pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setPreparedStatementParameter(args, pstmt);
            log.info("query = {}", sql);

            List<T> result = getQueryResult(rowMapper, pstmt);
            validateSingleResult(result);

            return result.get(0);
        } catch (SQLException e) {
            throw new DataAccessException("sql 실행 과정에서 문제가 발생하였습니다.", e);
        }
    }

    private <T> void validateSingleResult(List<T> result) {
        if(result.size() != 1) {
            throw new DataAccessException("데이터 개수가 1개가 아닙니다. (size: %d)".formatted(result.size()));
        }
    }

    private <T> List<T> getQueryResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                T object = rowMapper.mapRow(rs, rs.getRow());
                result.add(object);
            }

            return result;
        }
    }

    private void setPreparedStatementParameter(Object[] args, PreparedStatement pstmt) throws SQLException {
        for (int idx = 1; idx <= args.length; idx++) {
            pstmt.setObject(idx, args[idx - 1]);
        }
    }
}
