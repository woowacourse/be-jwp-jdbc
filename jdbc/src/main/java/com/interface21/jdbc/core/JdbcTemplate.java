package com.interface21.jdbc.core;

import com.interface21.dao.ResultNotSingleException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void write(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParameters(params, pstmt);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> readAll(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet resultSet = pstmt.executeQuery()) {

            log.debug("query : {}", sql);

            setParameters(params, pstmt);

            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.rowMap(resultSet));
            }

            return result;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T read(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> result = readAll(sql, rowMapper, params);
        if (result.size() > 1) {
            throw new ResultNotSingleException(result.size());
        }

        return  result.iterator().next();
    }

    private void setParameters(Object[] params, PreparedStatement pstmt) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            int sqlParamIndex = index + 1;
            pstmt.setObject(sqlParamIndex, params[index]);
        }
    }
}
