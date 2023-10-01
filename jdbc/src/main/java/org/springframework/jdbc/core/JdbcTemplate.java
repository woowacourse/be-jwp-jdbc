package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            try (final ResultSet rs = setPreparedStatementParameters(pstmt, args).executeQuery()) {
                final List<T> objects = new ArrayList<>();
                while (rs.next()) {
                    objects.add(rowMapper.mapRow(rs));
                }
                return objects;
            }
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            try (final ResultSet rs = setPreparedStatementParameters(pstmt, args).executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rowMapper.mapRow(rs));
                }
                return Optional.empty();
            }
        });
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, pstmt -> setPreparedStatementParameters(pstmt, args).executeUpdate());
    }

    private PreparedStatement setPreparedStatementParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
}
