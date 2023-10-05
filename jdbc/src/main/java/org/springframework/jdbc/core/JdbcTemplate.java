package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = makeStatement(conn, sql, parameters)
        ) {
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement makeStatement(final Connection connection, final String sql, final Object... parameters) {
        try {
            final PreparedStatement pstmt = connection.prepareStatement(sql);
            setParams(pstmt, parameters);
            return pstmt;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParams(final PreparedStatement pstmt, final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setString(i + 1, String.valueOf(parameters[i]));
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = makeStatement(conn, sql, parameters);
             ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);
            if (rs.next()) {
                return rowMapper.mapping(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowMapper.mapping(rs));
            }
            return list;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
