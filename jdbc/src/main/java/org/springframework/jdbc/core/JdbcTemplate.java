package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement prepareStatement = setUpPreparedStatement(conn, sql, args)
        ) {
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement prepareStatement = setUpPreparedStatement(conn, sql, args);
            ResultSet resultSet = prepareStatement.executeQuery();
        ) {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            throw new EmptyResultDataAccessException();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement prepareStatement = setUpPreparedStatement(conn, sql, args);
            ResultSet resultSet = prepareStatement.executeQuery();
        ) {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement setUpPreparedStatement(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement prepareStatement = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            prepareStatement.setObject(i + 1, args[i]);
        }
        return prepareStatement;
    }
}
