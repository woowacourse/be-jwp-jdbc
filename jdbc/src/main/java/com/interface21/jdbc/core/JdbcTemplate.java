package com.interface21.jdbc.core;

import com.interface21.jdbc.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql) {
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {

            log.debug("query : {}", sql);

            statement.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int parameterIndex = 0; parameterIndex < args.length; ++parameterIndex) {
                statement.setObject(parameterIndex + 1, args[parameterIndex]);
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(statement, args)) {
            log.debug("query : {}", sql);

            List<T> results = getResults(rs, rowMapper);
            validateSingleResult(results);

            return results.getFirst();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(statement, args)) {
            log.debug("query : {}", sql);

            return getResults(rs, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement statement, Object... args) throws SQLException {
        for (int parameterIndex = 0; parameterIndex < args.length; ++parameterIndex) {
            statement.setObject(parameterIndex + 1, args[parameterIndex]);
        }

        return statement.executeQuery();
    }

    private <T> List<T> getResults(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();

        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }

        return results;
    }

    private <T> void validateSingleResult(List<T> results) throws SQLException {
        if (results.isEmpty()) {
            throw new SQLException("쿼리 실행 결과가 1개이기를 기대했지만, 0개입니다.");
        }

        if (results.size() > 1) {
            throw new SQLException("쿼리 실행 결과가 1개이기를 기대했지만, 2개 이상입니다.");
        }
    }
}
